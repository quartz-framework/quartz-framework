package dev.pk7r.spigot.starter.core.bean.strategy;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;

import java.lang.reflect.Method;

@NoProxy
public interface BeanNameStrategy {

    String generateBeanName(Class<?> clazz);

    String generateBeanName(Method method);
}