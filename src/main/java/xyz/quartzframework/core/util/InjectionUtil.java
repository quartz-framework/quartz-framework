package xyz.quartzframework.core.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pacesys.reflect.Reflect;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import xyz.quartzframework.core.annotation.Inject;
import xyz.quartzframework.core.annotation.Property;
import xyz.quartzframework.core.bean.BeanProvider;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.property.PropertyPostProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

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
                Object obj;
                val beanProviderInstance = resolveBeanProviderDependency(pluginBeanFactory, parameter.getParameterizedType());
                if (beanProviderInstance != null) {
                    obj = beanProviderInstance;
                } else {
                    val optionalInstance = resolveOptionalDependency(pluginBeanFactory, parameter.getParameterizedType());
                    if (optionalInstance != null) {
                        obj = optionalInstance;
                    } else {
                        val collectionInstance = resolveCollectionDependency(pluginBeanFactory, type, parameter.getParameterizedType());
                        if (collectionInstance != null) {
                            obj = collectionInstance;
                        } else {
                            val namedInstance = BeanUtil.getNamedInstance(parameter);
                            if (namedInstance != null && !namedInstance.isEmpty() && pluginBeanFactory.containsBean(namedInstance)) {
                                obj = pluginBeanFactory.getBean(namedInstance, type);
                            } else if (pluginBeanFactory.containsBean(parameter.getName())) {
                                obj = pluginBeanFactory.getBean(parameter.getName(), type);
                            } else {
                                obj = pluginBeanFactory.getBean(type);
                            }
                        }
                    }
                }
                constructorParameterInstances[i] = obj;
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
                Object obj;
                val beanProviderInstance = resolveBeanProviderDependency(pluginBeanFactory, parameter.getParameterizedType());
                if (beanProviderInstance != null) {
                    obj = beanProviderInstance;
                } else {
                    val optionalInstance = resolveOptionalDependency(pluginBeanFactory, parameter.getParameterizedType());
                    if (optionalInstance != null) {
                        obj = optionalInstance;
                    } else {
                        val collectionInstance = resolveCollectionDependency(pluginBeanFactory, type, parameter.getParameterizedType());
                        if (collectionInstance != null) {
                            obj = collectionInstance;
                        } else {
                            val namedInstance = BeanUtil.getNamedInstance(parameter);
                            if (namedInstance != null && !namedInstance.isEmpty() && pluginBeanFactory.containsBean(namedInstance)) {
                                obj = pluginBeanFactory.getBean(namedInstance, type);
                            } else if (pluginBeanFactory.containsBean(parameter.getName())) {
                                obj = pluginBeanFactory.getBean(parameter.getName(), type);
                            } else {
                                obj = pluginBeanFactory.getBean(type);
                            }
                        }
                    }
                }
                parameterInstances[i] = obj;
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
                val beanProviderInstance = resolveBeanProviderDependency(pluginBeanFactory, field.getGenericType());
                if (beanProviderInstance != null) {
                    instance = beanProviderInstance;
                } else {
                    val optionalInstance = resolveOptionalDependency(pluginBeanFactory, field.getGenericType());
                    if (optionalInstance != null) {
                        instance = optionalInstance;
                    } else {
                        val collectionInstance = resolveCollectionDependency(pluginBeanFactory, type, field.getGenericType());
                        if (collectionInstance != null) {
                            instance = collectionInstance;
                        } else {
                            val namedInstance = BeanUtil.getNamedInstance(field);
                            if (namedInstance != null && !namedInstance.isEmpty() && pluginBeanFactory.containsBean(namedInstance)) {
                                instance = pluginBeanFactory.getBean(namedInstance, type);
                            } else if (pluginBeanFactory.containsBean(field.getName())) {
                                instance = pluginBeanFactory.getBean(field.getName(), type);
                            } else {
                                instance = pluginBeanFactory.getBean(type);
                            }
                        }
                    }
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

    @SneakyThrows
    private Object resolveCollectionDependency(PluginBeanFactory factory, Class<?> type, Type genericType) {
        if (Map.class.isAssignableFrom(type)) {
            val mapType = ResolvableType.forType(genericType).asMap();
            val keyType = mapType.getGeneric(0).resolve();
            val valueType = mapType.getGeneric(1).resolve();
            if (keyType != String.class || valueType == null) return null;
            return factory.getBeansOfType(valueType);
        }

        val resolvedType = ResolvableType.forType(genericType);
        val rawClass = resolvedType.resolve();
        if (rawClass == null || !Collection.class.isAssignableFrom(rawClass)) return null;

        val elementType = resolvedType.asCollection().getGeneric(0).resolve();
        if (elementType == null) return null;

        val values = factory.getBeansOfType(elementType).values();

        if (List.class.isAssignableFrom(type)) return new ArrayList<>(values);
        if (Set.class.isAssignableFrom(type)) return new HashSet<>(values);
        if (Queue.class.isAssignableFrom(type)) return new LinkedList<>(values);
        return new ArrayList<>(values);
    }

    @SneakyThrows
    private Object resolveOptionalDependency(PluginBeanFactory factory, Type genericType) {
        val resolvedType = ResolvableType.forType(genericType);
        val rawClass = resolvedType.resolve();
        if (rawClass == null || !Optional.class.isAssignableFrom(rawClass)) return null;

        val elementType = resolvedType.as(Optional.class).getGeneric(0).resolve();
        if (elementType == null) return Optional.empty();

        try {
            val bean = factory.getBean(elementType);
            return Optional.of(bean);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    @SneakyThrows
    private Object resolveBeanProviderDependency(PluginBeanFactory factory, Type genericType) {
        val resolvedType = ResolvableType.forType(genericType);
        val rawClass = resolvedType.resolve();
        if (rawClass == null || !BeanProvider.class.isAssignableFrom(rawClass)) return null;
        val elementType = resolvedType.as(BeanProvider.class).getGeneric(0).resolve();
        if (elementType == null) return null;
        return new BeanProvider<>(factory, elementType);
    }
}