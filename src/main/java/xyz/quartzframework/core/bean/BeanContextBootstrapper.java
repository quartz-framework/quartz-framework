package xyz.quartzframework.core.bean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.Listen;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.context.AbstractPluginContext;
import xyz.quartzframework.core.event.ContextLoadedEvent;

@NoProxy
@Slf4j
@RequiredArgsConstructor
@ContextBootstrapper
public class BeanContextBootstrapper {

    private final AbstractPluginContext pluginContext;

    private final PluginBeanDefinitionRegistry pluginBeanDefinitionRegistry;

    @Listen
    public void onLoad(ContextLoadedEvent event) {
        val context = event.getContext();
        if (!context.getId().equals(pluginContext.getId())) {
            return;
        }
        val beanFactory = context.getBeanFactory();
        pluginBeanDefinitionRegistry
                .getBeanDefinitions()
                .stream()
                .filter(b -> !b.isDeferred() && b.isSingleton())
                .filter(b -> !b.isInitialized())
                .forEach(b -> beanFactory.getBean(b.getName(), b.getType()));
        log.info("BeanFactory loaded with {} beans registered.", pluginBeanDefinitionRegistry.getBeanDefinitions().size());
    }
}