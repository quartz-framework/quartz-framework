package xyz.quartzframework.core.bean.definition;

import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Preferred;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.context.QuartzContext;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;

@NoProxy
@ContextBootstrapper
public class BeanDefinitionRegistryContextBootstrapper {

    @Provide
    @Preferred
    PluginBeanDefinitionRegistry beanDefinitionRegistry(QuartzContext<?> context) {
        return context.getBeanDefinitionRegistry();
    }

    @Provide
    @Preferred
    PluginBeanDefinitionBuilder beanDefinitionBuilder(QuartzContext<?> context) {
        return context.getBeanDefinitionBuilder();
    }
}