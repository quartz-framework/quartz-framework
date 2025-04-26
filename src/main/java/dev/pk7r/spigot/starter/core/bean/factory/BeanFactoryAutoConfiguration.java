package dev.pk7r.spigot.starter.core.bean.factory;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.Primary;
import dev.pk7r.spigot.starter.core.context.PluginContext;

@AutoConfiguration(force = true)
public class BeanFactoryAutoConfiguration {

    @Bean
    @Primary
    BeanFactory beanFactory(PluginContext context) {
        return context.getBeanFactory();
    }
}