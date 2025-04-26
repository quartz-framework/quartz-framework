package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.bean.BeanDefinition;

public interface ConditionalOnClassChecker extends ConditionChecker<ConditionalOnClassMetadata> {

    @Override
    boolean matches(ConditionalOnClassMetadata annotation, BeanDefinition beanDefinition);

}