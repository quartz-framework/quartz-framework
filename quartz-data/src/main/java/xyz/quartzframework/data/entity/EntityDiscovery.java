package xyz.quartzframework.data.entity;

import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.context.AbstractQuartzContext;
import xyz.quartzframework.data.annotation.DiscoverEntities;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EntityDiscovery {

    private final AbstractQuartzContext<?> context;

    private final Set<String> basePackages = new HashSet<>();

    private final Set<TypeMetadata> explicitEntities = new HashSet<>();

    private Map<String, Object> discoverers = Map.of();

    public static EntityDiscovery builder(AbstractQuartzContext<?> context) {
        return new EntityDiscovery(context);
    }

    public EntityDiscovery addBasePackage(String basePackage) {
        this.basePackages.add(basePackage);
        return this;
    }

    public EntityDiscovery addBasePackages(Collection<String> packages) {
        this.basePackages.addAll(packages);
        return this;
    }

    public EntityDiscovery addExplicitEntities(Collection<Class<?>> entities) {
        this.explicitEntities.addAll(entities.stream().map(e -> TypeMetadata.of(e, context.getClassLoader())).collect(Collectors.toSet()));
        return this;
    }

    public EntityDiscovery addDiscoverers(Map<String, Object> discoverers) {
        this.discoverers = discoverers;
        return this;
    }

    public Set<Class<?>> discover() {
        Set<TypeMetadata> entities = new HashSet<>(explicitEntities);
        val quartzApplication = context.getInformationMetadata();

        for (Object discoverer : discoverers.values()) {
            DiscoverEntities config = discoverer.getClass().getAnnotation(DiscoverEntities.class);
            if (config != null) {
                entities.addAll(Arrays.stream(config.value()).map(c -> TypeMetadata.of(c, context.getClassLoader())).toList());
                Collections.addAll(basePackages, config.basePackages());
            }
        }

        for (String pkg : basePackages) {
            entities.addAll(TypeMetadata.scan(
                new String[]{pkg},
                quartzApplication.exclude(),
                    c -> !c.isInterface() && c.getDeclaredFieldInfo()
                            .stream()
                            .anyMatch(f -> f.hasAnnotation(Id.class) || f.hasAnnotation(Identity.class)),
                    type -> !type.isAnnotation(),
                    quartzApplication.verbose(),
                    context.getClassLoader()
            ));
        }

        if (entities.isEmpty()) {
            String fallback = context.getPluginClass().getPackageName();
            entities.addAll(TypeMetadata.scan(
                new String[]{fallback},
                quartzApplication.exclude(),
                    c -> !c.isInterface() && c.getDeclaredFieldInfo()
                            .stream()
                            .anyMatch(f -> f.hasAnnotation(Id.class) || f.hasAnnotation(Identity.class)),
                    type -> !type.isAnnotation(),
                    quartzApplication.verbose(),
                    context.getClassLoader()
            ));
        }

        return entities.stream().map(TypeMetadata::getType).collect(Collectors.toSet());
    }
}