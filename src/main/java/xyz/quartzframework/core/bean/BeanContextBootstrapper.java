package xyz.quartzframework.core.bean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.ContextLoads;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.context.AbstractQuartzContext;

@NoProxy
@Slf4j
@RequiredArgsConstructor
@ContextBootstrapper
public class BeanContextBootstrapper {

    private final AbstractQuartzContext<?> pluginContext;

    private final PluginBeanDefinitionRegistry pluginBeanDefinitionRegistry;

    @ContextLoads
    public void onLoad() {
        val beanFactory = pluginContext.getBeanFactory();
        pluginBeanDefinitionRegistry
                .getBeanDefinitions()
                .stream()
                .filter(b -> !b.isDeferred() && b.isSingleton())
                .filter(b -> !b.isInitialized())
                .forEach(b -> beanFactory.getBean(b.getName(), b.getType()));
        log.info("BeanFactory loaded with {} beans registered.", pluginBeanDefinitionRegistry.getBeanDefinitions().size());
    }
}