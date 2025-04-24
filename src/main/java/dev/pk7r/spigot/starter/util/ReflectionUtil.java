package dev.pk7r.spigot.starter.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.pacesys.reflect.Reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class ReflectionUtil {

    @SafeVarargs
    public Set<Method> getMethods(Reflect.MethodType methodType, Class<?> clazz, Class<? extends Annotation>... annotations) {
        val methods = Reflect.on(clazz).methods(methodType).annotatedWith(annotations);
        return new HashSet<>(methods);
    }

    @SafeVarargs
    public Set<Field> getFields(Class<?> clazz, Class<? extends Annotation>... annotations) {
        val fields = Reflect.on(clazz).fields().annotatedWith(annotations);
        return new HashSet<>(fields);
    }
}