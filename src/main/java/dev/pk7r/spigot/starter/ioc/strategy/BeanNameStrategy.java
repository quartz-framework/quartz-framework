package dev.pk7r.spigot.starter.ioc.strategy;

public interface BeanNameStrategy {

    String generateBeanName(Class<?> clazz);

}