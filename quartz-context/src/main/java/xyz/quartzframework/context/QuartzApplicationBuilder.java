package xyz.quartzframework.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.quartzframework.Quartz;
import xyz.quartzframework.beans.definition.DefaultBeanDefinitionBuilder;
import xyz.quartzframework.beans.definition.DefaultBeanDefinitionRegistry;
import xyz.quartzframework.beans.definition.QuartzBeanDefinitionBuilder;
import xyz.quartzframework.beans.definition.QuartzBeanDefinitionRegistry;
import xyz.quartzframework.beans.factory.DefaultQuartzBeanFactory;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.beans.strategy.BeanNameStrategy;
import xyz.quartzframework.beans.strategy.DefaultBeanNameStrategy;

import java.net.URLClassLoader;

@RequiredArgsConstructor
public abstract class QuartzApplicationBuilder<T, C extends AbstractQuartzContext<T>> {

    @Getter
    private final Class<? extends Quartz<T>> pluginClass;

    @Getter
    private final Quartz<T> pluginInstance;

    private URLClassLoader classLoader;

    private BeanNameStrategy beanNameStrategy = new DefaultBeanNameStrategy();

    private QuartzBeanDefinitionBuilder beanDefinitionBuilder;

    private QuartzBeanDefinitionRegistry beanDefinitionRegistry;

    private QuartzBeanFactory beanFactory;

    public QuartzApplicationBuilder<T, C> classLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public QuartzApplicationBuilder<T, C> beanNameStrategy(BeanNameStrategy strategy) {
        this.beanNameStrategy = strategy;
        return this;
    }

    public QuartzApplicationBuilder<T, C> beanFactory(QuartzBeanFactory factory) {
        this.beanFactory = factory;
        return this;
    }

    public QuartzApplicationBuilder<T, C> beanDefinitionRegistry(QuartzBeanDefinitionRegistry registry) {
        this.beanDefinitionRegistry = registry;
        return this;
    }

    public QuartzApplicationBuilder<T, C> beanDefinitionBuilder(QuartzBeanDefinitionBuilder builder) {
        this.beanDefinitionBuilder = builder;
        return this;
    }

    public void run(C context) {
        if (pluginInstance == null) throw new IllegalStateException("Plugin instance must be set");
        if (classLoader == null) classLoader = (URLClassLoader) pluginInstance.getPlugin().getClass().getClassLoader();
        if (beanDefinitionRegistry == null) beanDefinitionRegistry = new DefaultBeanDefinitionRegistry(classLoader);
        if (beanFactory == null) beanFactory = new DefaultQuartzBeanFactory(classLoader, beanDefinitionRegistry, beanNameStrategy);
        if (beanDefinitionBuilder == null) beanDefinitionBuilder = new DefaultBeanDefinitionBuilder(beanFactory, beanNameStrategy);
        context.setBeanFactory(beanFactory);
        context.setBeanDefinitionBuilder(beanDefinitionBuilder);
        context.setBeanDefinitionRegistry(beanDefinitionRegistry);
        context.setBeanNameStrategy(beanNameStrategy);
        context.setQuartz(pluginInstance);
        context.setClassLoader(classLoader);
        context.start(pluginInstance);
    }
}