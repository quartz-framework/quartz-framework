package xyz.quartzframework.core.bean.registry;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.lang.NonNull;
import xyz.quartzframework.core.bean.PluginBeanDefinition;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public interface PluginBeanDefinitionRegistry extends BeanDefinitionRegistry {

    @Override
    PluginBeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    PluginBeanDefinition getBeanDefinition(Class<?> requiredType);

    PluginBeanDefinition getBeanDefinition(String beanName, Class<?> requiredType);

    boolean containsBeanDefinition(String beanName, Class<?> requiredType);

    void defineBeans(Class<?> clazz);

    @Override
    void registerBeanDefinition(@NonNull String beanName, @NonNull BeanDefinition beanDefinition);

    void unregisterBeanDefinition(UUID id);

    void registerSingletonBeanDefinition(Object instance);

    void registerSingletonBeanDefinition(Class<?> clazz, Object instance);

    void registerSingletonBeanDefinition(String beanName, Class<?> clazz, Object instance);

    Set<PluginBeanDefinition> getBeanDefinitions();

    Set<PluginBeanDefinition> getBeanDefinitionsByType(Class<?> requiredType);

    <T> void updateBeanInstance(PluginBeanDefinition pluginBeanDefinition, T instance);

    Predicate<PluginBeanDefinition> filterBeanDefinition(Class<?> requiredType);

    @Override
    default void registerAlias(String name, String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isAlias(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    default String[] getAliases(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void removeAlias(String alias) {
        throw new UnsupportedOperationException();
    }
}