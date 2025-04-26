package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.bean.BeanDefinition;

public interface ConditionalOnValueChecker extends ConditionChecker<ConditionalOnValueMetadata> {

    @Override
    boolean matches(ConditionalOnValueMetadata annotation, BeanDefinition beanDefinition);

}