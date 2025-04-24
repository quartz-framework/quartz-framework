package dev.pk7r.spigot.starter.strategy;

import java.lang.reflect.Method;

public interface BeanNameStrategy {

    String generateBeanName(Class<?> clazz);

    String generateBeanName(Method method);
}