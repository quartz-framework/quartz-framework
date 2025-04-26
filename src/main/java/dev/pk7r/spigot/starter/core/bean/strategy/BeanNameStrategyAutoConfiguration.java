package dev.pk7r.spigot.starter.core.bean.strategy;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.Primary;
import dev.pk7r.spigot.starter.core.context.PluginContext;

@AutoConfiguration(force = true)
public class BeanNameStrategyAutoConfiguration {

    @Bean
    @Primary
    BeanNameStrategy beanDefinitionRegistry(PluginContext context) {
        return context.getBeanNameStrategy();
    }
}