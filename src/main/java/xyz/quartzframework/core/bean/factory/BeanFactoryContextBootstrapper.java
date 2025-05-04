package xyz.quartzframework.core.bean.factory;

import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Preferred;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.context.QuartzContext;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;

@NoProxy
@ContextBootstrapper
public class BeanFactoryContextBootstrapper {

    @Provide
    @Preferred
    PluginBeanFactory beanFactory(QuartzContext<?> context) {
        return context.getBeanFactory();
    }
}