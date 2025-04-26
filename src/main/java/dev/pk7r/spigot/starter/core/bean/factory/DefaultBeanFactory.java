package dev.pk7r.spigot.starter.core.bean.factory;

import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.bean.BeanDefinition;
import dev.pk7r.spigot.starter.core.bean.BeanScope;
import dev.pk7r.spigot.starter.core.bean.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.bean.strategy.BeanNameStrategy;
import dev.pk7r.spigot.starter.core.exception.BeanCreationException;
import dev.pk7r.spigot.starter.core.util.BeanUtil;
import dev.pk7r.spigot.starter.core.util.InjectionUtil;
import dev.pk7r.spigot.starter.core.util.ReflectionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.pacesys.reflect.Reflect;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultBeanFactory implements BeanFactory {

    @Getter
    private final BeanDefinitionRegistry registry;

    private final BeanNameStrategy beanNameStrategy;

    @Override
    @SneakyThrows
    public <T> T getBean(Class<T> requiredType) {
        val beanDefinition = registry.getBeanDefinition(requiredType);
        return getInstance(beanDefinition);
    }

    @Override
    @SneakyThrows
    public <T> T getBean(String beanName, Class<T> requiredType) {
        val beanDefinition = registry.getBeanDefinition(beanName, requiredType);
        return getInstance(beanDefinition);
    }

    @Override
    public boolean containsBean(String beanName, Class<?> requiredType) {
        return registry.containsBeanDefinition(beanName, requiredType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getBeansOfType(Class<T> requiredType) {
        return registry.getBeanDefinitionsByType(requiredType)
                .stream()
                .map(beanDefinition -> beanDefinition.isInternalBean() ? (T) getBean(beanDefinition.getName(), beanDefinition.getLiteralType()) :
                        (T) getBean(beanDefinition.getName(), beanDefinition.getType()))
                .collect(Collectors.toSet());
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T getInstance(BeanDefinition beanDefinition) {
        if (beanDefinition.getScope().equals(BeanScope.SINGLETON) && beanDefinition.getInstance() != null) {
            return (T) beanDefinition.getInstance();
        } else if (beanDefinition.getScope().equals(BeanScope.SINGLETON)) {
            return registry.updateBeanInstance(beanDefinition, createInstance(beanDefinition));
        } else {
            return createInstance(beanDefinition);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(BeanDefinition beanDefinition) {
        if (beanDefinition.isInternalBean()) {
            return (T) InjectionUtil.newInstance(this, beanDefinition.getLiteralType());
        }
        val literalType = beanDefinition.getLiteralType();
        val methods = ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, literalType, Bean.class);
        val method = methods
                .stream()
                .filter(k -> {
                    val nameCandidate = beanNameStrategy.generateBeanName(k);
                    if (BeanUtil.hasNamedInstance(k)) {
                        return nameCandidate.equals(BeanUtil.getNamedInstance(k));
                    } else {
                        return beanDefinition.getName().equals(nameCandidate);
                    }
                })
                .findFirst()
                .orElse(null);
        if (method != null) {
            return InjectionUtil.newInstance(this, method);
        }
        throw new BeanCreationException("Cannot create instance of " + beanDefinition.getName());
    }
}