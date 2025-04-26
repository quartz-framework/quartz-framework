package dev.pk7r.spigot.starter.core.util;

import dev.pk7r.spigot.starter.core.annotation.*;
import dev.pk7r.spigot.starter.core.bean.BeanScope;
import dev.pk7r.spigot.starter.core.condition.ConditionalOnClassMetadata;
import dev.pk7r.spigot.starter.core.condition.ConditionalOnMissingClassMetadata;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class BeanUtil {

    public boolean isBean(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Bean.class);
    }

    public boolean isAutoConfiguration(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(AutoConfiguration.class);
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
        return isInjectable(annotatedElement, new HashSet<>());
    }

    private boolean isInjectable(AnnotatedElement annotatedElement, Set<Class<?>> visited) {
        if (annotatedElement.isAnnotationPresent(Injectable.class)) {
            return true;
        }
        for (Annotation annotation : annotatedElement.getAnnotations()) {
            Class<?> annotationType = annotation.annotationType();
            if (visited.contains(annotationType)) {
                continue;
            }
            visited.add(annotationType);
            if (isInjectable(annotationType, visited)) {
                return true;
            }
        }
        return false;
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

    public Set<String> getConditionalOnClass(ConditionalOnClassMetadata annotation) {
        if (annotation == null) return new HashSet<>();
        val classes = Arrays.stream(annotation.getClasses()).map(Class::getName).collect(Collectors.toSet());
        classes.addAll(Arrays.stream(annotation.getClassNames()).collect(Collectors.toSet()));
        return classes;
    }

    public Set<String> getConditionalOnMissingClass(ConditionalOnMissingClassMetadata annotation) {
        if (annotation == null) return new HashSet<>();
        val classes = Arrays.stream(annotation.getClasses()).map(Class::getName).collect(Collectors.toSet());
        classes.addAll(Arrays.stream(annotation.getClassNames()).collect(Collectors.toSet()));
        return classes;
    }
}