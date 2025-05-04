package xyz.quartzframework.core.context;

import xyz.quartzframework.core.QuartzApplication;
import xyz.quartzframework.core.QuartzPlugin;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;

import java.util.UUID;

@NoProxy
public interface QuartzContext<T> {

    UUID getId();

    void start(QuartzPlugin<T> quartzPlugin);

    void close();

    default boolean isVerbose() {
        return getQuartzApplication().verbose();
    }

    QuartzPlugin<T> getQuartzPlugin();

    QuartzApplication getQuartzApplication();

    PluginBeanFactory getBeanFactory();

    BeanNameStrategy getBeanNameStrategy();

    PluginBeanDefinitionRegistry getBeanDefinitionRegistry();
}