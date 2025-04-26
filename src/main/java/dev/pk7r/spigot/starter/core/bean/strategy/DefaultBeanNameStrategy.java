package dev.pk7r.spigot.starter.core.bean.strategy;

import java.lang.reflect.Method;

public class DefaultBeanNameStrategy implements BeanNameStrategy {

    @Override
    public String generateBeanName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    @Override
    public String generateBeanName(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }
}