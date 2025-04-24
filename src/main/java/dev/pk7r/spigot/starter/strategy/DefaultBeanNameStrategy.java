package dev.pk7r.spigot.starter.strategy;

import java.lang.reflect.Method;
import java.util.Objects;

public class DefaultBeanNameStrategy implements BeanNameStrategy {

    private static BeanNameStrategy beanNameStrategy;

    public static BeanNameStrategy getInstance() {
        if (Objects.isNull(beanNameStrategy)) {
            beanNameStrategy = new DefaultBeanNameStrategy();
        }
        return beanNameStrategy;
    }

    @Override
    public String generateBeanName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    @Override
    public String generateBeanName(Method method) {
        return "%s#%s".formatted(method.getName(), generateBeanName(method.getDeclaringClass()));
    }
}