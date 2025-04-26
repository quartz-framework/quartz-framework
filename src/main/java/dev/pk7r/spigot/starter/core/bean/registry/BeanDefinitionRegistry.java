package dev.pk7r.spigot.starter.core.bean.registry;

import dev.pk7r.spigot.starter.core.bean.BeanDefinition;
import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public interface BeanDefinitionRegistry {

    BeanDefinition getBeanDefinition(Class<?> requiredType);

    BeanDefinition getBeanDefinition(String beanName, Class<?> requiredType);

    boolean containsBeanDefinition(String beanName, Class<?> requiredType);

    List<BeanDefinition> registerBeanDefinition(BeanFactory beanFactory, Class<?> clazz);

    void unregisterBeanDefinition(UUID id);

    void registerSingletonBeanDefinition(Object instance);

    void registerSingletonBeanDefinition(Class<?> clazz, Object instance);

    void registerSingletonBeanDefinition(String beanName, Class<?> clazz, Object instance);

    Set<BeanDefinition> getBeanDefinitions();

    Set<BeanDefinition> getBeanDefinitionsByType(Class<?> requiredType);

    <T> T updateBeanInstance(BeanDefinition beanDefinition, T instance);

    Predicate<BeanDefinition> filterBeanDefinition(Class<?> requiredType);
}