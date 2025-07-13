package xyz.quartzframework.beans.support;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pacesys.reflect.Reflect;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import xyz.quartzframework.Inject;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.beans.support.exception.BeanCreationException;
import xyz.quartzframework.config.Property;
import xyz.quartzframework.config.PropertyPostProcessor;
import xyz.quartzframework.config.PropertySupplier;
import xyz.quartzframework.util.CollectionUtil;
import xyz.quartzframework.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class BeanInjector {

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T newInstance(QuartzBeanFactory quartzBeanFactory, Class<T> clazz) {
        val constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new BeanCreationException("No public constructors found for class: " + clazz.getName());
        }
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
                constructorParameterInstances[i] = resolveProperty(quartzBeanFactory, parameter.getParameterizedType(), value);
            } else {
                Object obj;
                val beanProviderInstance = resolveBeanProviderDependency(quartzBeanFactory, parameter.getParameterizedType());
                if (beanProviderInstance != null) {
                    obj = beanProviderInstance;
                } else {
                    val collectionInstance = resolveCollectionDependency(quartzBeanFactory, type, parameter.getParameterizedType());
                    if (collectionInstance != null) {
                        obj = collectionInstance;
                    } else {
                        val namedInstance = BeanUtil.getNamedInstance(parameter);
                        if (namedInstance != null && !namedInstance.isEmpty() && quartzBeanFactory.containsBean(namedInstance)) {
                            obj = quartzBeanFactory.getBean(namedInstance, type);
                        } else if (quartzBeanFactory.containsBean(parameter.getName())) {
                            obj = quartzBeanFactory.getBean(parameter.getName(), type);
                        } else {
                            obj = quartzBeanFactory.getBean(type);
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
    public <T> T newInstance(QuartzBeanFactory quartzBeanFactory, Method method) {
        method.setAccessible(true);
        val parameters = method.getParameters();
        val parameterInstances = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            val parameter = parameters[i];
            val type = parameter.getType();
            val value = parameter.getAnnotation(Property.class);
            if (value != null) {
                parameterInstances[i] = resolveProperty(quartzBeanFactory, parameter.getParameterizedType(), value);
            } else {
                Object obj;
                val beanProviderInstance = resolveBeanProviderDependency(quartzBeanFactory, parameter.getParameterizedType());
                if (beanProviderInstance != null) {
                    obj = beanProviderInstance;
                } else {
                    val collectionInstance = resolveCollectionDependency(quartzBeanFactory, type, parameter.getParameterizedType());
                    if (collectionInstance != null) {
                        obj = collectionInstance;
                    } else {
                        val namedInstance = BeanUtil.getNamedInstance(parameter);
                        if (namedInstance != null && !namedInstance.isEmpty() && quartzBeanFactory.containsBean(namedInstance)) {
                            obj = quartzBeanFactory.getBean(namedInstance, type);
                        } else if (quartzBeanFactory.containsBean(parameter.getName())) {
                            obj = quartzBeanFactory.getBean(parameter.getName(), type);
                        } else {
                            obj = quartzBeanFactory.getBean(type);
                        }
                    }
                }
                parameterInstances[i] = obj;
            }
        }
        val clazz = method.getDeclaringClass();
        val registry = quartzBeanFactory.getRegistry();
        val beanDefinition = registry.getBeanDefinition(clazz);
        Object bean = beanDefinition.getInstance();
        if (bean == null) {
            bean = BeanInjector.newInstance(quartzBeanFactory, clazz);
            registry.updateBeanInstance(beanDefinition, bean);
        }
        return (T) method.invoke(bean, parameterInstances);
    }

    @SneakyThrows
    public void recursiveInjection(QuartzBeanFactory quartzBeanFactory, Object bean) {
        if (bean == null) return;
        val target = AopUtils.getTargetClass(bean);
        for (val field : CollectionUtil.reorder(ReflectionUtil.getFields(target, Inject.class, Autowired.class, Property.class))) {
            field.setAccessible(true);
            Object instance;
            val type = field.getType();
            val value = field.getAnnotation(Property.class);
            if (value != null) {
                instance = resolveProperty(quartzBeanFactory, field.getGenericType(), value);
            } else {
                val beanProviderInstance = resolveBeanProviderDependency(quartzBeanFactory, field.getGenericType());
                if (beanProviderInstance != null) {
                    instance = beanProviderInstance;
                } else {
                    val collectionInstance = resolveCollectionDependency(quartzBeanFactory, type, field.getGenericType());
                    if (collectionInstance != null) {
                        instance = collectionInstance;
                    } else {
                        val namedInstance = BeanUtil.getNamedInstance(field);
                        if (namedInstance != null && !namedInstance.isEmpty() && quartzBeanFactory.containsBean(namedInstance)) {
                            instance = quartzBeanFactory.getBean(namedInstance, type);
                        } else if (quartzBeanFactory.containsBean(field.getName())) {
                            instance = quartzBeanFactory.getBean(field.getName(), type);
                        } else {
                            instance = quartzBeanFactory.getBean(type);
                        }
                    }
                }
            }
            val realTarget = BeanInjector.unwrapIfProxy(bean);
            field.set(realTarget, instance);
            recursiveInjection(quartzBeanFactory, instance);
        }
        for (val method : CollectionUtil.reorder(ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, target, Inject.class))) {
            newInstance(quartzBeanFactory, method);
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
    private Object resolveCollectionDependency(QuartzBeanFactory factory, Class<?> type, Type genericType) {
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
    private Object resolveBeanProviderDependency(QuartzBeanFactory factory, Type genericType) {
        val resolvedType = ResolvableType.forType(genericType);
        val rawClass = resolvedType.resolve();
        if (rawClass == null || !BeanProvider.class.isAssignableFrom(rawClass)) return null;
        val elementType = resolvedType.as(BeanProvider.class).getGeneric(0).resolve();
        if (elementType == null) return null;
        return new BeanProvider<>(factory, elementType);
    }

    @SneakyThrows
    private Object resolveProperty(QuartzBeanFactory factory, Type genericType, Property annotation) {
        val postProcessor = factory.getBean(PropertyPostProcessor.class);
        val type = ResolvableType.forType(genericType);
        val rawClass = type.resolve();

        if (rawClass != null && Supplier.class.isAssignableFrom(rawClass)) {
            val generic = type.as(Supplier.class).getGeneric(0).resolve();
            if (generic == null) {
                throw new IllegalArgumentException("Could not resolve Supplier<T> generic type for property: " + annotation.value());
            }
            return new PropertySupplier<>(postProcessor, annotation.source(), annotation.value(), generic);
        }
        return postProcessor.process(annotation.value(), annotation.source(), rawClass);
    }
}