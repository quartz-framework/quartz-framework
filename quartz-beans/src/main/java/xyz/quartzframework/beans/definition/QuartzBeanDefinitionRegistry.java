package xyz.quartzframework.beans.definition;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.lang.NonNull;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public interface QuartzBeanDefinitionRegistry extends BeanDefinitionRegistry {

    @Override
    QuartzBeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    QuartzBeanDefinition getBeanDefinition(Class<?> requiredType);

    QuartzBeanDefinition getBeanDefinition(String beanName, Class<?> requiredType);

    QuartzBeanDefinition getBeanDefinition(TypeMetadata metadata);

    QuartzBeanDefinition getBeanDefinition(String beanName, TypeMetadata metadata);

    boolean containsBeanDefinition(String beanName, Class<?> requiredType);

    boolean containsBeanDefinition(String beanName, TypeMetadata metadata);

    @Override
    void registerBeanDefinition(@NonNull String beanName, @NonNull BeanDefinition beanDefinition);

    void unregisterBeanDefinition(UUID id);

    Set<QuartzBeanDefinition> getBeanDefinitions();

    Set<QuartzBeanDefinition> getBeanDefinitionsByType(Class<?> requiredType);

    Set<QuartzBeanDefinition> getBeanDefinitionsByType(TypeMetadata metadata);

    <T> void updateBeanInstance(QuartzBeanDefinition quartzBeanDefinition, T instance);

    Predicate<QuartzBeanDefinition> filterBeanDefinition(Class<?> requiredType);

    Predicate<QuartzBeanDefinition> filterBeanDefinition(TypeMetadata metadata);

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