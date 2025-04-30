package xyz.quartzframework.core.bean.factory;

import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Preferred;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.context.PluginContext;

@NoProxy
@ContextBootstrapper
public class BeanFactoryContextBootstrapper {

    @Provide
    @Preferred
    PluginBeanFactory beanFactory(PluginContext context) {
        return context.getBeanFactory();
    }
}