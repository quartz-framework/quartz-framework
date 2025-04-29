package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.bean.PluginBeanDefinition;
import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;

@FunctionalInterface
public interface ConditionEvaluator {

    boolean evaluate(PluginBeanDefinition beanDefinition, PluginBeanFactory beanFactory);

}