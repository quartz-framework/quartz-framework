package dev.pk7r.spigot.starter.core.bean.factory;

import dev.pk7r.spigot.starter.core.bean.registry.BeanDefinitionRegistry;

import java.util.Collection;

public interface BeanFactory {

    <T> T getBean(Class<T> requiredType);

    <T> T getBean(String beanName, Class<T> requiredType);

    <T> Collection<T> getBeansOfType(Class<T> requiredType);

    boolean containsBean(String beanName, Class<?> requiredType);

    BeanDefinitionRegistry getRegistry();

}