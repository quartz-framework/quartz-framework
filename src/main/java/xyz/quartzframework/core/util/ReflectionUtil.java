package xyz.quartzframework.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.pacesys.reflect.Reflect;
import org.pacesys.reflect.types.Predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class ReflectionUtil {

    @SafeVarargs
    public Set<Method> getMethods(Reflect.MethodType methodType, Class<?> clazz, Class<? extends Annotation>... annotations) {
        val methodFinder = Reflect.on(clazz).methods(methodType);
        val methods = methodFinder.annotatedWith(annotations);
        return new HashSet<>(methods);
    }

    public Set<Method> getMethods(Reflect.MethodType methodType, Class<?> clazz, Predicate<Method> predicate) {
        val methodFinder = Reflect.on(clazz).methods(methodType);
        val methods = methodFinder.match(predicate);
        return new HashSet<>(methods);
    }

    @SafeVarargs
    public Set<Field> getFields(Class<?> clazz, Class<? extends Annotation>... annotations) {
        val fieldFinder = Reflect.on(clazz).fields();
        val fields = annotations.length == 0 ? fieldFinder.all() : fieldFinder.annotatedWith(annotations);
        return new HashSet<>(fields);
    }
}