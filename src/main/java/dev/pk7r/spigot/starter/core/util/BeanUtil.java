package dev.pk7r.spigot.starter.core.util;

import dev.pk7r.spigot.starter.core.annotation.*;
import dev.pk7r.spigot.starter.core.property.PropertyPostProcessor;
import dev.pk7r.spigot.starter.core.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.core.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.model.BeanScope;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class BeanUtil {

    public boolean isBean(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Bean.class);
    }

    public boolean isPrimary(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Primary.class);
    }

    public boolean isLazy(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Lazy.class);
    }

    public boolean isSingleton(AnnotatedElement annotatedElement) {
        val isScopePresent = annotatedElement.isAnnotationPresent(Scope.class);
        return !isScopePresent || annotatedElement.getAnnotation(Scope.class).value() == BeanScope.SINGLETON;
    }

    public boolean isInjectable(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Injectable.class);
    }

    public boolean isConditionalOnClass(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(ConditionalOnClass.class);
    }

    public boolean hasNamedInstance(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(NamedInstance.class);
    }

    public String getNamedInstance(AnnotatedElement annotatedElement) {
        if (hasNamedInstance(annotatedElement)) {
            val value = annotatedElement.getAnnotation(NamedInstance.class).value();
            return value.isEmpty() ? "" : value;
        }
        return "";
    }

    public Set<String> getConditionalOnClass(AnnotatedElement annotatedElement) {
        return new HashSet<>(Arrays.asList(annotatedElement.getAnnotation(ConditionalOnClass.class).classNames()));
    }

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
        val bean = beanFactory.getBean(method.getDeclaringClass());
        return (T) method.invoke(bean, parameterInstances);
    }
}