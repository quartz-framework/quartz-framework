package dev.pk7r.spigot.starter.ioc.strategy;

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
}