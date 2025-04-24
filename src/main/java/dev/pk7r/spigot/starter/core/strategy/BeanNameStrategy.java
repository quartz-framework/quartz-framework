package dev.pk7r.spigot.starter.core.strategy;

import java.lang.reflect.Method;

public interface BeanNameStrategy {

    String generateBeanName(Class<?> clazz);

    String generateBeanName(Method method);
}