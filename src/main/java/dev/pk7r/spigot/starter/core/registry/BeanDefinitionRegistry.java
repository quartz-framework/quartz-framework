package dev.pk7r.spigot.starter.core.registry;

import dev.pk7r.spigot.starter.core.model.BeanDefinition;

import java.util.Set;

public interface BeanDefinitionRegistry {

    BeanDefinition getBeanDefinition(Class<?> requiredType);

    BeanDefinition getBeanDefinition(String beanName, Class<?> requiredType);

    boolean containsBeanDefinition(String beanName, Class<?> requiredType);

    void registerBeanDefinition(Class<?> clazz);

    void registerSingletonBeanDefinition(String beanName, Class<?> clazz, Object instance);

    Set<BeanDefinition> getBeanDefinitions();

    Set<BeanDefinition> getBeanDefinitionsByType(Class<?> requiredType);

    <T> T updateBeanInstance(BeanDefinition beanDefinition, T instance);
}