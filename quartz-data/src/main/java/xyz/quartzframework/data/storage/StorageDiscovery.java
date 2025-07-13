package xyz.quartzframework.data.storage;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.context.AbstractQuartzContext;
import xyz.quartzframework.data.annotation.DiscoverStorages;
import xyz.quartzframework.data.annotation.Storage;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StorageDiscovery {

    private final AbstractQuartzContext<?> context;

    private final Set<String> basePackages = new HashSet<>();

    private final Set<TypeMetadata> explicitStorages = new HashSet<>();

    private Map<String, Object> discoverers = Map.of();

    public static StorageDiscovery builder(AbstractQuartzContext<?> context) {
        return new StorageDiscovery(context);
    }

    public StorageDiscovery addBasePackage(String basePackage) {
        this.basePackages.add(basePackage);
        return this;
    }

    public StorageDiscovery addBasePackages(Collection<String> packages) {
        this.basePackages.addAll(packages);
        return this;
    }

    public StorageDiscovery addExplicitStorages(Collection<Class<?>> storages) {
        this.explicitStorages.addAll(storages.stream().map(s -> TypeMetadata.of(s, context.getClassLoader())).collect(Collectors.toSet()));
        return this;
    }

    public StorageDiscovery addDiscoverers(Map<String, Object> discoverers) {
        this.discoverers = discoverers;
        return this;
    }

    public Set<Class<?>> discover() {
        Set<TypeMetadata> storages = new HashSet<>(explicitStorages);
        val quartzApplication = context.getInformationMetadata();

        for (Object discoverer : discoverers.values()) {
            DiscoverStorages config = discoverer.getClass().getAnnotation(DiscoverStorages.class);
            if (config != null) {
                storages.addAll(Arrays.stream(config.value()).map(c -> TypeMetadata.of(c, context.getClassLoader())).collect(Collectors.toSet()));
                Collections.addAll(basePackages, config.basePackages());
            }
        }

        for (String pkg : basePackages) {
            storages.addAll(TypeMetadata.scan(
                new String[]{pkg},
                quartzApplication.exclude(),
                    c -> c.isInterface() && c.hasAnnotation(Storage.class),
                    type -> !type.isAnnotation(),
                    quartzApplication.verbose(),
                    context.getClassLoader()
            ));
        }

        if (storages.isEmpty()) {
            String fallback = context.getPluginClass().getPackageName();
            storages.addAll(TypeMetadata.scan(
                new String[]{fallback},
                quartzApplication.exclude(),
                    c -> c.isInterface() && c.hasAnnotation(Storage.class),
                    type -> !type.isAnnotation(),
                quartzApplication.verbose(),
                context.getClassLoader()
            ));
        }

        return storages.stream().map(TypeMetadata::getType).collect(Collectors.toSet());
    }
}