package dev.pk7r.spigot.starter.core.bean;

import co.aikar.commands.BaseCommand;
import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.bean.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.command.CommandFactory;
import dev.pk7r.spigot.starter.core.condition.*;
import dev.pk7r.spigot.starter.core.event.ContextLoadedEvent;
import dev.pk7r.spigot.starter.core.listener.ListenerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@AutoConfiguration(force = true)
public class BeanAutoConfiguration implements Listener {

    private final BeanDefinitionRegistry beanDefinitionRegistry;

    private final ListenerFactory listenerFactory;

    private final CommandFactory commandFactory;

    private final ConditionalOnClassChecker conditionalOnClassConditionChecker;

    private final ConditionalOnMissingClassChecker conditionalOnMissingClassConditionChecker;

    private final ConditionalOnBeanChecker conditionalOnBeanConditionChecker;

    private final ConditionalOnMissingBeanChecker conditionalOnMissingBeanConditionChecker;

    private final ConditionalOnValueChecker conditionalOnValueConditionChecker;

    private final ConditionalOnAnnotationChecker conditionalOnAnnotationConditionChecker;

    @EventHandler
    public void onLoad(ContextLoadedEvent event) {
        val context = event.getContext();
        val toUnregister = beanDefinitionRegistry
                .getBeanDefinitions()
                .stream()
                .filter(b -> !matches(b))
                .collect(Collectors.toList());
        toUnregister.forEach(this::unregisterBean);
        val unregisteredAmount = toUnregister.size();
        log.info("Found {} beans that conditions not met.", unregisteredAmount);
        val beanFactory = context.getBeanFactory();
        beanDefinitionRegistry
                .getBeanDefinitions()
                .stream()
                .filter(b -> !b.isLazy() && b.getScope().equals(BeanScope.SINGLETON))
                .forEach(b -> beanFactory.getBean(b.getName(), b.getType()));
        log.info("BeanFactory loaded with {} beans registered.", beanDefinitionRegistry.getBeanDefinitions().size());
    }

    private boolean matches(BeanDefinition beanDefinition) {
        return matchesAnnotation(beanDefinition)
                && matchesBean(beanDefinition)
                && matchesMissingBean(beanDefinition)
                && matchesValue(beanDefinition)
                && matchesClass(beanDefinition)
                && matchesMissingClass(beanDefinition);
    }

    private boolean matchesAnnotation(BeanDefinition beanDefinition) {
        return conditionalOnAnnotationConditionChecker.matches(beanDefinition.getConditionalOnAnnotation(), beanDefinition);
    }

    private boolean matchesBean(BeanDefinition beanDefinition) {
        return conditionalOnBeanConditionChecker.matches(beanDefinition.getConditionalOnBean(), beanDefinition);
    }

    private boolean matchesMissingBean(BeanDefinition beanDefinition) {
        return conditionalOnMissingBeanConditionChecker.matches(beanDefinition.getConditionalOnMissingBean(), beanDefinition);
    }

    private boolean matchesValue(BeanDefinition beanDefinition) {
        return conditionalOnValueConditionChecker.matches(beanDefinition.getConditionalOnValue(), beanDefinition);
    }

    private boolean matchesClass(BeanDefinition beanDefinition) {
        return conditionalOnClassConditionChecker.matches(beanDefinition.getConditionalOnClass(), beanDefinition);
    }

    private boolean matchesMissingClass(BeanDefinition beanDefinition) {
        return conditionalOnMissingClassConditionChecker.matches(beanDefinition.getConditionalOnMissingClass(), beanDefinition);
    }

    private void unregisterBean(BeanDefinition beanDefinition) {
        beanDefinitionRegistry.unregisterBeanDefinition(beanDefinition.getId());
        val instance = beanDefinition.getInstance();
        if (instance instanceof BaseCommand) {
            val command = (BaseCommand) instance;
            commandFactory.unregisterCommand(command);
        }
        if (instance instanceof Listener) {
            val listener = (Listener) instance;
            listenerFactory.unregisterEvents(listener);
        }
    }
}