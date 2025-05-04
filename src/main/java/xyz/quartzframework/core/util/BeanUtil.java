package xyz.quartzframework.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import xyz.quartzframework.core.bean.annotation.*;
import xyz.quartzframework.core.bean.annotation.scope.Prototype;
import xyz.quartzframework.core.bean.annotation.scope.Singleton;
import xyz.quartzframework.core.condition.Evaluators;
import xyz.quartzframework.core.condition.annotation.Environment;
import xyz.quartzframework.core.context.annotation.*;

import javax.annotation.ManagedBean;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class BeanUtil {

    public String getDescription(AnnotatedElement annotatedElement) {
        val describe = annotatedElement.getAnnotation(Describe.class);
        if (describe != null) {
            return describe.value();
        }
        return "";
    }

    public String[] discovery(AnnotatedElement annotatedElement) {
        val describe = annotatedElement.getAnnotation(Discover.class);
        if (describe != null) {
            return describe.basePackages();
        }
        return new String[0];
    }

    public boolean isProviderMethod(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Provide.class);
    }

    public boolean isAspect(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Aspect.class);
    }

    public boolean isContextBootstrapper(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(ContextBootstrapper.class)|| annotatedElement.isAnnotationPresent(Configurable.class);
    }

    public boolean isBootstrapper(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Bootstrapper.class);
    }

    public boolean isConfigurer(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Configurer.class);
    }

    public boolean isPreferred(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Preferred.class);
    }

    public boolean isSecondary(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Secondary.class);
    }

    public boolean isDeferred(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Deferred.class);
    }

    public boolean isProxy(AnnotatedElement annotatedElement) {
        return !annotatedElement.isAnnotationPresent(NoProxy.class);
    }

    public boolean isSingleton(AnnotatedElement annotatedElement) {
        val prototype = annotatedElement.getAnnotation(Prototype.class);
        return prototype == null || annotatedElement.isAnnotationPresent(Singleton.class);
    }

    public boolean isPrototype(AnnotatedElement annotatedElement) {
        val prototype = annotatedElement.getAnnotation(Prototype.class);
        return prototype != null;
    }

    public boolean isInjectable(AnnotatedElement annotatedElement) {
        return isInjectable(annotatedElement, new HashSet<>());
    }

    private boolean isInjectable(AnnotatedElement annotatedElement, Set<Class<?>> visited) {
        if (annotatedElement.isAnnotationPresent(Injectable.class) || annotatedElement.isAnnotationPresent(ManagedBean.class)) {
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

    public int getOrder(AnnotatedElement annotatedElement) {
        val order = annotatedElement.getAnnotation(Order.class);
        if (order == null) {
            val priority = annotatedElement.getAnnotation(Priority.class);
            if (priority == null) {
                return Integer.MAX_VALUE;
            }
            return priority.value();
        }
        return order.value();
    }

    public List<String> getEnvironments(AnnotatedElement annotatedElement) {
        val annotation = annotatedElement.getAnnotation(Environment.class);
        if (annotation == null) {
            return Evaluators.DEFAULT_PROFILES;
        }
        val value = annotation.value();
        if (value.length == 0) {
            return Evaluators.DEFAULT_PROFILES;
        }
        return Arrays.stream(value).collect(Collectors.toList());
    }

    public <T extends AnnotatedElement> List<T> reorder(List<T> unorderedElements) {
        return unorderedElements
                .stream()
                .sorted(Comparator.comparingInt(BeanUtil::getOrder))
                .collect(Collectors.toList());
    }

    public Set<Class<?>> getImports(AnnotatedElement annotatedElement) {
        return getImports(annotatedElement, new HashSet<>(), new HashSet<>());
    }

    private Set<Class<?>> getImports(AnnotatedElement annotatedElement, Set<Class<?>> visited, Set<Class<?>> imports) {
        if (annotatedElement.isAnnotationPresent(External.class)) {
            External externalAnnotation = annotatedElement.getAnnotation(External.class);
            if (externalAnnotation != null) {
                Collections.addAll(imports, externalAnnotation.value());
            }
        }
        for (Annotation annotation : annotatedElement.getAnnotations()) {
            Class<?> annotationType = annotation.annotationType();
            if (visited.contains(annotationType)) {
                continue;
            }
            visited.add(annotationType);
            getImports(annotationType, visited, imports);
        }
        return imports;
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