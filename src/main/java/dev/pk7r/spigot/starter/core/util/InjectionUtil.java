package dev.pk7r.spigot.starter.core.util;

import dev.pk7r.spigot.starter.core.annotation.Inject;
import dev.pk7r.spigot.starter.core.annotation.Value;
import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.core.property.PropertyPostProcessor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@Slf4j
@UtilityClass
public class InjectionUtil {

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T newInstance(BeanFactory beanFactory, Class<T> clazz) {
        val constructors = clazz.getConstructors();
        var selectedConstructor = constructors[0];
        if (constructors.length > 1) {
            for (Constructor<?> constructor : constructors) {
                if (BeanUtil.isInjectable(constructor)) {
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
            val value = parameter.getAnnotation(Value.class);
            if (value != null) {
                val environmentPostProcessor = beanFactory.getBean(PropertyPostProcessor.class);
                val targetValue = environmentPostProcessor.process(value.value(), value.source(), type);
                constructorParameterInstances[i] = targetValue;
            } else {
                if (BeanUtil.hasNamedInstance(parameter)) {
                    val namedInstance = BeanUtil.getNamedInstance(parameter);
                    if (!beanFactory.containsBean(namedInstance, type)) {
                        throw new BeanNotFoundException(String.format("No beans %s found for %s",
                                namedInstance,
                                type.getSimpleName()));
                    }
                    val obj = beanFactory.getBean(namedInstance, type);
                    constructorParameterInstances[i] = obj;
                } else {
                    val obj = beanFactory.getBean(type);
                    constructorParameterInstances[i] = obj;
                }
            }
        }
        return (T) selectedConstructor.newInstance(constructorParameterInstances);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T newInstance(BeanFactory beanFactory, Method method) {
        method.setAccessible(true);
        val parameters = method.getParameters();
        val parameterInstances = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            val parameter = parameters[i];
            val type = parameter.getType();
            val value = parameter.getAnnotation(Value.class);
            if (value != null) {
                val environmentPostProcessor = beanFactory.getBean(PropertyPostProcessor.class);
                val targetValue = environmentPostProcessor.process(value.value(), value.source(), type);
                parameterInstances[i] = targetValue;
            } else {
                if (BeanUtil.hasNamedInstance(parameter)) {
                    val namedInstance = BeanUtil.getNamedInstance(parameter);
                    if (!beanFactory.containsBean(namedInstance, type)) {
                        throw new BeanNotFoundException(String.format("No beans %s found for %s",
                                namedInstance,
                                type.getSimpleName()));
                    }
                    val obj = beanFactory.getBean(namedInstance, type);
                    parameterInstances[i] = obj;
                } else {
                    val obj = beanFactory.getBean(type);
                    parameterInstances[i] = obj;
                }
            }
        }
        val clazz = method.getDeclaringClass();
        val registry = beanFactory.getRegistry();
        val beanDefinition = registry.getBeanDefinition(clazz);
        Object bean = beanDefinition.getInstance();
        if (bean == null) {
            bean = InjectionUtil.newInstance(beanFactory, clazz);
            registry.updateBeanInstance(beanDefinition, bean);
        }
        return (T) method.invoke(bean, parameterInstances);
    }

    @SneakyThrows
    public void recursiveInjection(BeanFactory beanFactory, Class<?> clazz) {
        for (val field : ReflectionUtil.getFields(clazz, Inject.class, Value.class)) {
            field.setAccessible(true);
            Object instance;
            val type = field.getType();
            val value = field.getAnnotation(Value.class);
            if (value != null) {
                val environmentPostProcessor = beanFactory.getBean(PropertyPostProcessor.class);
                instance = environmentPostProcessor.process(value.value(), value.source(), type);
            } else {
                if (BeanUtil.hasNamedInstance(field)) {
                    if (!beanFactory.containsBean(BeanUtil.getNamedInstance(field), type)) {
                        throw new BeanNotFoundException(String.format("No beans %s found for %s",
                                BeanUtil.getNamedInstance(field),
                                type.getSimpleName()));
                    }
                    instance = beanFactory.getBean(BeanUtil.getNamedInstance(field), type);
                } else instance = beanFactory.getBean(type);
            }
            field.set(beanFactory.getBean(clazz), instance);
            recursiveInjection(beanFactory, instance.getClass());
        }
    }
}