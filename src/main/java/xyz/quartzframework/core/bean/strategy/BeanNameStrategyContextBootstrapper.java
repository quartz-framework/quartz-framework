package xyz.quartzframework.core.bean.strategy;

import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Preferred;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.context.QuartzContext;

@NoProxy
@ContextBootstrapper
public class BeanNameStrategyContextBootstrapper {

    @Provide
    @Preferred
    BeanNameStrategy beanDefinitionRegistry(QuartzContext<?> context) {
        return context.getBeanNameStrategy();
    }
}