package xyz.quartzframework.core.context;

import org.bukkit.plugin.Plugin;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.PluginApplication;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;

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