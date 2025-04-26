package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.bean.BeanDefinition;

public interface ConditionalOnMissingBeanChecker extends ConditionChecker<ConditionalOnMissingBeanMetadata> {

    @Override
    boolean matches(ConditionalOnMissingBeanMetadata annotation, BeanDefinition beanDefinition);

}