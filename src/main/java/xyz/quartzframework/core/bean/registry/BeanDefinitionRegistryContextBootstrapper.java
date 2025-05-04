package xyz.quartzframework.core.bean.registry;

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
}