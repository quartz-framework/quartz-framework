package xyz.quartzframework.core.bean.strategy;

import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Preferred;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.context.QuartzContext;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;

@NoProxy
@ContextBootstrapper
public class BeanNameStrategyContextBootstrapper {

    @Provide
    @Preferred
    BeanNameStrategy beanDefinitionRegistry(QuartzContext<?> context) {
        return context.getBeanNameStrategy();
    }
}