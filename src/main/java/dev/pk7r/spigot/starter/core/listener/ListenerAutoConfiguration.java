package dev.pk7r.spigot.starter.core.listener;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnMissingBean;
import org.bukkit.plugin.Plugin;

@AutoConfiguration(force = true)
public class ListenerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ListenerFactory.class)
    ListenerFactory listenerFactory(Plugin plugin) {
        return new DefaultListenerFactory(plugin);
    }
}