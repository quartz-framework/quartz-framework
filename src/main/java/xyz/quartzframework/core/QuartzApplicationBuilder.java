package xyz.quartzframework.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.quartzframework.core.bean.definition.DefaultBeanDefinitionBuilder;
import xyz.quartzframework.core.bean.definition.DefaultBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.definition.PluginBeanDefinitionBuilder;
import xyz.quartzframework.core.bean.definition.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.factory.DefaultPluginBeanFactory;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.bean.strategy.DefaultBeanNameStrategy;
import xyz.quartzframework.core.context.AbstractQuartzContext;

import java.net.URLClassLoader;

@RequiredArgsConstructor
public abstract class QuartzApplicationBuilder<T, C extends AbstractQuartzContext<T>> {

    @Getter
    private final Class<? extends QuartzPlugin<T>> pluginClass;

    @Getter
    private final QuartzPlugin<T> pluginInstance;

    private URLClassLoader classLoader;

    private BeanNameStrategy beanNameStrategy = new DefaultBeanNameStrategy();

    private PluginBeanDefinitionBuilder beanDefinitionBuilder;

    private PluginBeanDefinitionRegistry beanDefinitionRegistry;

    private PluginBeanFactory beanFactory;

    public QuartzApplicationBuilder<T, C> classLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public QuartzApplicationBuilder<T, C> beanNameStrategy(BeanNameStrategy strategy) {
        this.beanNameStrategy = strategy;
        return this;
    }

    public QuartzApplicationBuilder<T, C> beanFactory(PluginBeanFactory factory) {
        this.beanFactory = factory;
        return this;
    }

    public QuartzApplicationBuilder<T, C> beanDefinitionRegistry(PluginBeanDefinitionRegistry registry) {
        this.beanDefinitionRegistry = registry;
        return this;
    }

    public QuartzApplicationBuilder<T, C> beanDefinitionBuilder(PluginBeanDefinitionBuilder builder) {
        this.beanDefinitionBuilder = builder;
        return this;
    }

    public void run(C context) {
        if (pluginInstance == null) throw new IllegalStateException("Plugin instance must be set");
        if (classLoader == null) classLoader = (URLClassLoader) pluginInstance.getPlugin().getClass().getClassLoader();
        if (beanDefinitionRegistry == null) beanDefinitionRegistry = new DefaultBeanDefinitionRegistry(classLoader);
        if (beanFactory == null) beanFactory = new DefaultPluginBeanFactory(classLoader, beanDefinitionRegistry, beanNameStrategy);
        if (beanDefinitionBuilder == null) beanDefinitionBuilder = new DefaultBeanDefinitionBuilder(beanFactory, beanNameStrategy);
        context.setBeanFactory(beanFactory);
        context.setBeanDefinitionBuilder(beanDefinitionBuilder);
        context.setBeanDefinitionRegistry(beanDefinitionRegistry);
        context.setBeanNameStrategy(beanNameStrategy);
        context.setQuartzPlugin(pluginInstance);
        context.setClassLoader(classLoader);
        pluginInstance.setContext(context);
        context.start(pluginInstance);
    }
}