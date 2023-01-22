package dev.pk7r.spigot.starter.ioc.factory.bean;

import dev.pk7r.spigot.starter.ioc.model.BeanDefinition;
import dev.pk7r.spigot.starter.ioc.model.BeanScope;
import dev.pk7r.spigot.starter.ioc.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.ioc.util.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DefaultBeanFactory implements ListableBeanFactory, BeanFactory {

    BeanDefinitionRegistry beanDefinitionRegistry;

    @Override
    @SneakyThrows
    public <T> T getBean(Class<T> requiredType) {
        val beanDefinition = beanDefinitionRegistry.getBeanDefinition(requiredType);
        return getInstance(beanDefinition);
    }

    @Override
    @SneakyThrows
    public <T> T getBean(String beanName, Class<T> requiredType) {
        val beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName, requiredType);
        return getInstance(beanDefinition);
    }

    @Override
    public boolean containsBean(String beanName, Class<?> requiredType) {
        return beanDefinitionRegistry.containsBeanDefinition(beanName, requiredType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getBeansOfType(Class<T> requiredType) {
        return beanDefinitionRegistry.getBeanDefinitionsByType(requiredType)
                .stream()
                .map(beanDefinition -> (T) getBean(beanDefinition.getName(), beanDefinition.getType()))
                .collect(Collectors.toSet());
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T getInstance(BeanDefinition beanDefinition) {
        if (BeanUtil.isBean(beanDefinition.getLiteralType())) {
            val instance = (Method) beanDefinition.getInstance();
            return (T) instance.invoke(beanDefinition.getLiteralType().newInstance());
        }
        if (beanDefinition.getScope().equals(BeanScope.SINGLETON)) {
            return (T) beanDefinition.getInstance();
        } else {
            return (T) beanDefinition.getLiteralType().newInstance();
        }
    }
}