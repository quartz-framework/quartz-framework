package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.bean.BeanDefinition;

public interface ConditionalOnAnnotationChecker extends ConditionChecker<ConditionalOnAnnotationMetadata> {

    @Override
    boolean matches(ConditionalOnAnnotationMetadata annotation, BeanDefinition beanDefinition);

}