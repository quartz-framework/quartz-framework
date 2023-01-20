package dev.pk7r.spigot.starter.ioc.util;

import dev.pk7r.spigot.starter.ioc.annotation.*;
import dev.pk7r.spigot.starter.ioc.model.BeanScope;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.AnnotatedElement;
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

    public boolean isSingleton(AnnotatedElement annotatedElement) {
        val isScopePresent = annotatedElement.isAnnotationPresent(Scope.class);
        return !isScopePresent || annotatedElement.getAnnotation(Scope.class).value() == BeanScope.SINGLETON;
    }

    public boolean isInjectable(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Injectable.class) ||
                Arrays.stream(annotatedElement.getAnnotations())
                        .anyMatch(annotation -> annotation.getClass().isAnnotationPresent(Injectable.class));
    }

    public boolean isConditionalOnClass(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(ConditionalOnClass.class);
    }

    public boolean hasNamedInstance(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(NamedInstance.class);
    }

    public String getNamedInstance(AnnotatedElement annotatedElement) {
        val value = annotatedElement.getAnnotation(NamedInstance.class).value();
        return value.isEmpty() ? "" : value;
    }

    public Set<String> getConditionalOnClass(AnnotatedElement annotatedElement) {
        return new HashSet<>(Arrays.asList(annotatedElement.getAnnotation(ConditionalOnClass.class).classNames()));
    }
}