package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.bean.BeanDefinition;

@FunctionalInterface
public interface ConditionChecker<T> {

    boolean matches(T annotation, BeanDefinition beanDefinition);

}