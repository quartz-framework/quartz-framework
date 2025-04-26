package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.bean.BeanDefinition;

public interface ConditionalOnMissingClassChecker extends ConditionChecker<ConditionalOnMissingClassMetadata> {

    @Override
    boolean matches(ConditionalOnMissingClassMetadata annotation, BeanDefinition beanDefinition);

}