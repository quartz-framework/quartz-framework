package dev.pk7r.spigot.starter.core.bean.registry;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.Primary;
import dev.pk7r.spigot.starter.core.context.PluginContext;

@AutoConfiguration(force = true)
public class BeanDefinitionRegistryAutoConfiguration {

    @Bean
    @Primary
    BeanDefinitionRegistry beanDefinitionRegistry(PluginContext context) {
        return context.getBeanDefinitionRegistry();
    }
}