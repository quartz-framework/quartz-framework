package xyz.quartzframework.beans.definition;

import lombok.val;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import xyz.quartzframework.Injectable;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.condition.Evaluators;
import xyz.quartzframework.beans.definition.metadata.MethodMetadata;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.annotation.*;
import xyz.quartzframework.beans.support.annotation.condition.Environment;
import xyz.quartzframework.beans.support.annotation.scope.Prototype;
import xyz.quartzframework.beans.support.annotation.scope.Singleton;
import xyz.quartzframework.ordered.Priority;
import xyz.quartzframework.stereotype.Bootstrapper;
import xyz.quartzframework.stereotype.Configurer;
import xyz.quartzframework.stereotype.ContextBootstrapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface QuartzBeanDefinitionBuilder {

    @Nullable
    QuartzBeanDefinition create(TypeMetadata typeMetadata, @Nullable QuartzBeanDefinition delegate, boolean external);

    @Nullable
    default QuartzBeanDefinition create(TypeMetadata typeMetadata) {
        return create(typeMetadata, false);
    }

    @Nullable
    default QuartzBeanDefinition create(TypeMetadata typeMetadata, boolean external) {
        return create(typeMetadata, null, external);
    }

    default boolean isAspect(TypeMetadata metadata) {
        return metadata.hasAnnotation(Aspect.class);
    }

    default boolean isProxied(TypeMetadata metadata) {
        return !metadata.hasAnnotation(NoProxy.class);
    }

    default boolean isConfigurer(TypeMetadata metadata) {
        return metadata.hasAnnotation(Configurer.class);
    }

    default boolean isBootstrapper(TypeMetadata metadata) {
        return metadata.hasAnnotation(Bootstrapper.class);
    }

    default boolean isContextBootstrapper(TypeMetadata metadata) {
        return metadata.hasAnnotation(ContextBootstrapper.class);
    }

    default boolean isDeferred(TypeMetadata metadata) {
        return metadata.hasAnnotation(Deferred.class);
    }

    default boolean isPreferred(TypeMetadata metadata) {
        return metadata.hasAnnotation(Preferred.class);
    }

    default boolean isSecondary(TypeMetadata metadata) {
        return metadata.hasAnnotation(Secondary.class);
    }

    default boolean isSingleton(TypeMetadata metadata) {
        return metadata.hasAnnotation(Singleton.class) || !isPrototype(metadata);
    }

    default boolean isPrototype(TypeMetadata metadata) {
        return metadata.hasAnnotation(Prototype.class);
    }

    default boolean isNamedInstance(TypeMetadata metadata) {
        return metadata.hasAnnotation(NamedInstance.class);
    }

    default List<String> getEnvironments(TypeMetadata metadata) {
        if (!metadata.hasAnnotation(Environment.class)) {
            return Evaluators.DEFAULT_PROFILES;
        }
        val annotation = metadata.getAnnotation(Environment.class);
        return annotation
                .map(a -> {
                    String[] value = a.getAttribute("value", String[].class);
                    if (value == null || value.length == 0) {
                        return Evaluators.DEFAULT_PROFILES;
                    }
                    return Arrays.asList(value);
                })
                .orElse(Evaluators.DEFAULT_PROFILES);
    }

    default String getDescription(TypeMetadata metadata) {
        return metadata
                .getAnnotation(BeanDescription.class)
                .map(d -> d.getAttribute("value", String.class))
                .orElse("");
    }

    default List<MethodMetadata> findMethods(@Nullable TypeMetadata metadata) {
        if (metadata == null) {
            return new ArrayList<>();
        }
        return metadata.getMethods();
    }

    default int getOrder(TypeMetadata metadata) {
        return metadata
                .getAnnotation(Priority.class)
                .or(() -> metadata.getAnnotation(Order.class))
                .map(a -> a.getAttribute("value", int.class))
                .orElse(Ordered.HIGHEST_PRECEDENCE);
    }

    default String[] discovery(TypeMetadata metadata) {
        return metadata.getAnnotation(Discover.class)
                .map(a -> a.getAttribute("basePackages", String[].class))
                .orElse(new String[0]);
    }

    default boolean isInjectable(TypeMetadata metadata) {
        return metadata.hasAnnotation(Injectable.class);
    }

    default Set<TypeMetadata> getImports(TypeMetadata metadata) {
        return metadata.getAnnotations().stream()
                .filter(a -> a.getName().equals(External.class.getName()))
                .flatMap(a -> {
                    Class<?>[] values = a.getAttribute("value", Class[].class);
                    if (values == null) return Stream.empty();
                    return Arrays.stream(values)
                            .filter(Objects::nonNull)
                            .map(c -> TypeMetadata.of(c, metadata.getClassLoader()));
                })
                .collect(Collectors.toSet());
    }
}