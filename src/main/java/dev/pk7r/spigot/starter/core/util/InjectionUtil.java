package dev.pk7r.spigot.starter.core.util;

import dev.pk7r.spigot.starter.core.annotation.Inject;
import dev.pk7r.spigot.starter.core.annotation.Property;
import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;
import dev.pk7r.spigot.starter.core.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.core.property.PropertyPostProcessor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import org.pacesys.reflect.Reflect;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

@Slf4j
@UtilityClass
public class InjectionUtil {

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T newInstance(PluginBeanFactory pluginBeanFactory, Class<T> clazz) {
        val constructors = clazz.getConstructors();
        var selectedConstructor = constructors[0];
        if (constructors.length > 1) {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(Inject.class) || constructor.isAnnotationPresent(Autowired.class)) {
                    selectedConstructor = constructor;
                    break;
                }
            }
        }
        selectedConstructor.setAccessible(true);
        val parameters = selectedConstructor.getParameters();
        val constructorParameterInstances = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            val parameter = parameters[i];
            val type = parameter.getType();
            val value = parameter.getAnnotation(Property.class);
            if (value != null) {
                val environmentPostProcessor = pluginBeanFactory.getBean(PropertyPostProcessor.class);
                val targetValue = environmentPostProcessor.process(value.value(), value.source(), type);
                constructorParameterInstances[i] = targetValue;
            } else {
                if (BeanUtil.hasNamedInstance(parameter)) {
                    val namedInstance = BeanUtil.getNamedInstance(parameter);
                    if (!pluginBeanFactory.containsBean(namedInstance)) {
                        throw new BeanNotFoundException(String.format("No beans %s found for %s",
                                namedInstance,
                                type.getSimpleName()));
                    }
                    val obj = pluginBeanFactory.getBean(namedInstance, type);
                    constructorParameterInstances[i] = obj;
                } else {
                    val obj = pluginBeanFactory.getBean(type);
                    constructorParameterInstances[i] = obj;
                }
            }
        }
        return (T) selectedConstructor.newInstance(constructorParameterInstances);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T newInstance(PluginBeanFactory pluginBeanFactory, Method method) {
        method.setAccessible(true);
        val parameters = method.getParameters();
        val parameterInstances = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            val parameter = parameters[i];
            val type = parameter.getType();
            val value = parameter.getAnnotation(Property.class);
            if (value != null) {
                val environmentPostProcessor = pluginBeanFactory.getBean(PropertyPostProcessor.class);
                val targetValue = environmentPostProcessor.process(value.value(), value.source(), type);
                parameterInstances[i] = targetValue;
            } else {
                if (BeanUtil.hasNamedInstance(parameter)) {
                    val namedInstance = BeanUtil.getNamedInstance(parameter);
                    if (!pluginBeanFactory.containsBean(namedInstance)) {
                        throw new BeanNotFoundException(String.format("No beans %s found for %s",
                                namedInstance,
                                type.getSimpleName()));
                    }
                    val obj = pluginBeanFactory.getBean(namedInstance, type);
                    parameterInstances[i] = obj;
                } else {
                    val obj = pluginBeanFactory.getBean(type);
                    parameterInstances[i] = obj;
                }
            }
        }
        val clazz = method.getDeclaringClass();
        val registry = pluginBeanFactory.getRegistry();
        val beanDefinition = registry.getBeanDefinition(clazz);
        Object bean = beanDefinition.getInstance();
        if (bean == null) {
            bean = InjectionUtil.newInstance(pluginBeanFactory, clazz);
            registry.updateBeanInstance(beanDefinition, bean);
        }
        return (T) method.invoke(bean, parameterInstances);
    }

    @SneakyThrows
    public void recursiveInjection(PluginBeanFactory pluginBeanFactory, Object bean) {
        if (bean == null) return;
        val target = AopUtils.getTargetClass(bean);
        for (val field : BeanUtil.reorder(new ArrayList<>(ReflectionUtil.getFields(target, Inject.class, Autowired.class, Property.class)))) {
            field.setAccessible(true);
            Object instance;
            val type = field.getType();
            val value = field.getAnnotation(Property.class);
            if (value != null) {
                val environmentPostProcessor = pluginBeanFactory.getBean(PropertyPostProcessor.class);
                instance = environmentPostProcessor.process(value.value(), value.source(), type);
            } else {
                if (BeanUtil.hasNamedInstance(field)) {
                    if (!pluginBeanFactory.containsBean(BeanUtil.getNamedInstance(field))) {
                        throw new BeanNotFoundException(String.format("No beans %s found for %s",
                                BeanUtil.getNamedInstance(field),
                                type.getSimpleName()));
                    }
                    instance = pluginBeanFactory.getBean(BeanUtil.getNamedInstance(field), type);
                } else {
                    instance = pluginBeanFactory.getBean(type);
                }
            }
            val realTarget = InjectionUtil.unwrapIfProxy(bean);
            field.set(realTarget, instance);
            recursiveInjection(pluginBeanFactory, instance);
        }
        for (val method : BeanUtil.reorder(new ArrayList<>(ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, target, Inject.class)))) {
            newInstance(pluginBeanFactory, method);
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T unwrapIfProxy(T bean) {
        if (bean == null) return null;
        if (AopUtils.isAopProxy(bean)) {
            return (T) ((Advised) bean).getTargetSource().getTarget();
        }
        return bean;
    }
}