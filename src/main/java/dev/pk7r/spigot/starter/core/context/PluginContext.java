package dev.pk7r.spigot.starter.core.context;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.PluginApplication;
import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;
import dev.pk7r.spigot.starter.core.bean.registry.PluginBeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.bean.strategy.BeanNameStrategy;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

@NoProxy
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

    PluginBeanFactory getBeanFactory();

    BeanNameStrategy getBeanNameStrategy();

    PluginBeanDefinitionRegistry getBeanDefinitionRegistry();
}