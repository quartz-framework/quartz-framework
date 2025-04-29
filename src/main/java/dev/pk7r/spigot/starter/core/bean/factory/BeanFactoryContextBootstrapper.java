package dev.pk7r.spigot.starter.core.bean.factory;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Preferred;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.context.PluginContext;

@NoProxy
@ContextBootstrapper
public class BeanFactoryContextBootstrapper {

    @Provide
    @Preferred
    PluginBeanFactory beanFactory(PluginContext context) {
        return context.getBeanFactory();
    }
}