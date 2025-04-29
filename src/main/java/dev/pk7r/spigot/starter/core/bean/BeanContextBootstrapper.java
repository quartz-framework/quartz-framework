package dev.pk7r.spigot.starter.core.bean;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;
import dev.pk7r.spigot.starter.core.bean.registry.PluginBeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.condition.*;
import dev.pk7r.spigot.starter.core.context.AbstractPluginContext;
import dev.pk7r.spigot.starter.core.event.ContextLoadedEvent;
import dev.pk7r.spigot.starter.core.listener.ListenerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.stream.Collectors;

@NoProxy
@Slf4j
@RequiredArgsConstructor
@ContextBootstrapper
public class BeanContextBootstrapper implements Listener {

    private final AbstractPluginContext pluginContext;

    private final PluginBeanDefinitionRegistry pluginBeanDefinitionRegistry;

    @EventHandler
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