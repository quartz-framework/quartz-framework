package xyz.quartzframework.core.condition;

import xyz.quartzframework.core.bean.PluginBeanDefinition;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;

@FunctionalInterface
public interface ConditionEvaluator {

    boolean evaluate(PluginBeanDefinition beanDefinition, PluginBeanFactory beanFactory);

}