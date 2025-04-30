package xyz.quartzframework.core.bean.strategy;

import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Preferred;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.context.PluginContext;

@NoProxy
@ContextBootstrapper
public class BeanNameStrategyContextBootstrapper {

    @Provide
    @Preferred
    BeanNameStrategy beanDefinitionRegistry(PluginContext context) {
        return context.getBeanNameStrategy();
    }
}