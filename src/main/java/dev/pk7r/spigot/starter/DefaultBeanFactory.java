package dev.pk7r.spigot.starter;

import dev.pk7r.spigot.starter.annotation.NamedInstance;
import dev.pk7r.spigot.starter.exception.BeanCreationException;
import dev.pk7r.spigot.starter.factory.bean.BeanFactory;
import dev.pk7r.spigot.starter.factory.bean.ListableBeanFactory;
import dev.pk7r.spigot.starter.model.BeanDefinition;
import dev.pk7r.spigot.starter.model.BeanScope;
import dev.pk7r.spigot.starter.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.strategy.DefaultBeanNameStrategy;
import dev.pk7r.spigot.starter.util.BeanUtil;
import dev.pk7r.spigot.starter.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.pacesys.reflect.Reflect;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
class DefaultBeanFactory implements ListableBeanFactory, BeanFactory {

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
        if (beanDefinition.getScope().equals(BeanScope.SINGLETON) && beanDefinition.getInstance() != null) {
            return (T) beanDefinition.getInstance();
        } else if (beanDefinition.getScope().equals(BeanScope.SINGLETON)) {
            return beanDefinitionRegistry.updateBeanInstance(beanDefinition, createInstance(beanDefinition));
        } else {
            return createInstance(beanDefinition);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(BeanDefinition beanDefinition) {
        if (beanDefinition.isInternalBean()) {
            return (T) BeanUtil.newInstance(this, beanDefinition.getLiteralType());
        } else {
            val literalType = beanDefinition.getLiteralType();
            val hasNamedInstance = beanDefinition.isNamedInstance();
            java.util.Set<Method> methods;
            if (hasNamedInstance) {
                methods = ReflectionUtil.getMethods(Reflect.MethodType.ALL, literalType, NamedInstance.class);
                val method = methods.stream().filter(m -> BeanUtil.getNamedInstance(m).equals(beanDefinition.getName())).findFirst().orElse(null);
                if (method != null) {
                    return BeanUtil.newInstance(this, method);
                }
            } else {
                methods = ReflectionUtil.getMethods(Reflect.MethodType.ALL, literalType);
                val method = methods.stream().filter(m -> {
                    val s = DefaultBeanNameStrategy.getInstance().generateBeanName(m);
                    return s.equals(beanDefinition.getName());
                }).findFirst().orElse(null);
                if (method != null) {
                    return BeanUtil.newInstance(this, method);
                }
            }
            throw new BeanCreationException("Cannot create instance of " + beanDefinition.getName());
        }
    }
}