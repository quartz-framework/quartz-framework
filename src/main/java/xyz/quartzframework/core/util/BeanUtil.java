package xyz.quartzframework.core.util;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import xyz.quartzframework.core.bean.annotation.Injectable;
import xyz.quartzframework.core.bean.annotation.NamedInstance;

import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class BeanUtil {


    public boolean isInjectable(ClassInfo classInfo) {
        return isInjectable(classInfo, new HashSet<>());
    }

    private boolean isInjectable(ClassInfo classInfo, Set<ClassInfo> visited) {
        if (classInfo.hasAnnotation(Injectable.class)) {
            return true;
        }
        for (AnnotationInfo annotation : classInfo.getAnnotationInfo()) {
            ClassInfo annotationType = annotation.getClassInfo();
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
        return annotatedElement.isAnnotationPresent(NamedInstance.class) || annotatedElement.isAnnotationPresent(Qualifier.class);
    }

    public String getNamedInstance(AnnotatedElement annotatedElement) {
        if (hasNamedInstance(annotatedElement)) {
            val namedInstance = annotatedElement.getAnnotation(NamedInstance.class);
            if (namedInstance == null) {
                val qualifier = annotatedElement.getAnnotation(Qualifier.class);
                if (qualifier == null) return "";
                val value = qualifier.value();
                return value.isEmpty() ? "" : value;
            }
            val value = namedInstance.value();
            return value.isEmpty() ? "" : value;
        }
        return "";
    }
}