package dev.pk7r.spigot.starter.core.property;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenBeanMissing;
import org.bukkit.plugin.Plugin;
import org.springframework.core.convert.ConversionService;

@NoProxy

@ContextBootstrapper
public class PropertyContextBootstrapper {

    @Provide
    @ActivateWhenBeanMissing(PropertySourceFactory.class)
    PropertySourceFactory propertySourceFactory(Plugin plugin) {
        return new DefaultPropertySourceFactory(plugin);
    }

    @Provide
    @ActivateWhenBeanMissing(PropertyPostProcessor.class)
    PropertyPostProcessor propertyPostProcessor(PropertySourceFactory propertySourceFactory, ConversionService conversionService) {
        return new DefaultPropertyPostProcessor(propertySourceFactory, conversionService);
    }
}