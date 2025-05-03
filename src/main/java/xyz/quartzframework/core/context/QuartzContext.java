package xyz.quartzframework.core.context;

import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.PluginApplication;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.QuartzPlugin;

import java.util.UUID;

@NoProxy
public interface QuartzContext<T> {

    UUID getId();

    void start(QuartzPlugin<T> quartzPlugin);

    void close();

    default boolean isVerbose() {
        return getPluginApplication().verbose();
    }

    QuartzPlugin<T> getQuartzPlugin();

    PluginApplication getPluginApplication();

    PluginBeanFactory getBeanFactory();

    BeanNameStrategy getBeanNameStrategy();

    PluginBeanDefinitionRegistry getBeanDefinitionRegistry();
}