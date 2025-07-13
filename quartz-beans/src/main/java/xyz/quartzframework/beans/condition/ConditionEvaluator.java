package xyz.quartzframework.beans.condition;

import xyz.quartzframework.beans.definition.QuartzBeanDefinition;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;

@FunctionalInterface
public interface ConditionEvaluator {

    boolean evaluate(QuartzBeanDefinition beanDefinition, QuartzBeanFactory beanFactory);

}