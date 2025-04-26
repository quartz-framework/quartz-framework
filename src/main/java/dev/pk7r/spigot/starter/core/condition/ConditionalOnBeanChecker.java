package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.bean.BeanDefinition;

public interface ConditionalOnBeanChecker extends ConditionChecker<ConditionalOnBeanMetadata> {

    @Override
    boolean matches(ConditionalOnBeanMetadata annotation, BeanDefinition beanDefinition);

}