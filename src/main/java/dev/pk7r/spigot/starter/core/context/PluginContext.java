package dev.pk7r.spigot.starter.core.context;

import dev.pk7r.spigot.starter.core.annotation.PluginApplication;
import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.bean.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.bean.strategy.BeanNameStrategy;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface PluginContext {

    UUID getId();

    Plugin getPlugin();

    void setPlugin(Plugin plugin);

    void start(Plugin plugin);

    void close();

    PluginApplication getPluginApplication();

    default boolean isVerbose() {
        return getPlugin().isEnabled();
    }

    BeanFactory getBeanFactory();

    BeanNameStrategy getBeanNameStrategy();

    BeanDefinitionRegistry getBeanDefinitionRegistry();
}