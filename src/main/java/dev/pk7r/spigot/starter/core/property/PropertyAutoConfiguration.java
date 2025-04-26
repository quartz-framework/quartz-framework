package dev.pk7r.spigot.starter.core.property;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnMissingBean;
import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;
import org.bukkit.plugin.Plugin;

@AutoConfiguration(force = true)
public class PropertyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PropertySourceFactory.class)
    PropertySourceFactory propertySourceFactory(Plugin plugin) {
        return new DefaultPropertySourceFactory(plugin);
    }

    @Bean
    @ConditionalOnMissingBean(PropertyPostProcessor.class)
    PropertyPostProcessor propertyPostProcessor(PropertySourceFactory propertySourceFactory, BeanFactory beanFactory) {
        return new DefaultPropertyPostProcessor(propertySourceFactory, beanFactory);
    }
}