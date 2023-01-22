package dev.pk7r.spigot.starter.ioc.registry;

import dev.pk7r.spigot.starter.ioc.context.DefaultPluginContext;
import dev.pk7r.spigot.starter.ioc.exception.BeanCreationException;
import dev.pk7r.spigot.starter.ioc.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.ioc.model.AbstractBean;
import dev.pk7r.spigot.starter.ioc.model.BeanDefinition;
import dev.pk7r.spigot.starter.ioc.model.BeanScope;
import dev.pk7r.spigot.starter.ioc.strategy.DefaultBeanNameStrategy;
import dev.pk7r.spigot.starter.ioc.util.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class DefaultBeanDefinitionRegistry implements BeanDefinitionRegistry {

    DefaultPluginContext pluginContext;

    private final Set<BeanDefinition> beanDefinitions = new HashSet<>();

    @Override
    public BeanDefinition getBeanDefinition(Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(beanDefinition -> beanDefinition.getLiteralType().equals(requiredType) || beanDefinition.getType().equals(requiredType))
                .findFirst()
                .orElseThrow(() -> new BeanNotFoundException("No beans found for " + requiredType.getSimpleName()));
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName, Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(beanDefinition -> beanDefinition.getLiteralType().equals(requiredType) || beanDefinition.getType().equals(requiredType))
                .filter(beanDefinition -> beanDefinition.getName().equals(beanName))
                .findFirst()
                .orElseThrow(() -> new BeanNotFoundException("No beans found for " + requiredType.getSimpleName()));
    }

    @Override
    public boolean containsBeanDefinition(String beanName, Class<?> requiredType) {
        return beanDefinitions.stream()
                .filter(beanDefinition -> beanDefinition.getLiteralType().equals(requiredType) || beanDefinition.getType().equals(requiredType))
                .anyMatch(beanDefinition -> beanDefinition.getName().equals(beanName));
    }

    @Override
    @SneakyThrows
    public void registerBeanDefinition(Class<?> clazz) {
        if (clazz.isInterface()) {
            throw new BeanCreationException("Bean type can't be an interface");
        }
        String beanName = null;
        if (BeanUtil.hasNamedInstance(clazz)) {
            beanName = BeanUtil.getNamedInstance(clazz);
        }
        if (Objects.isNull(beanName) || beanName.isEmpty()) {
            beanName = DefaultBeanNameStrategy.getInstance().generateBeanName(clazz);
        }
        val isBean = BeanUtil.isBean(clazz);
        Class<?> beanInterface = null;
        if (clazz.getInterfaces().length != 0) {
            beanInterface = clazz.getInterfaces()[0];
        }
        Class<?> requiredType = Objects.isNull(beanInterface) ? clazz : beanInterface;
        if (containsBeanDefinition(beanName, requiredType)) {
            throw new BeanCreationException("A bean with name %s of type %s has already been registered");
        }
        val isSingleton = BeanUtil.isSingleton(clazz);
        Object instance = null;
        Object classInstance = clazz.newInstance();
        if (isBean) {
            if (Objects.isNull(clazz.getSuperclass())) {
                throw new BeanCreationException("Beans annotated with @Bean must extends AbstractBean");
            }
            if (!clazz.getSuperclass().equals(AbstractBean.class)) {
                throw new BeanCreationException("Beans annotated with @Bean must extends AbstractBean");
            }
            try {
                val method = clazz.getMethod("instance");
                if (method.getParameterCount() > 0) {
                    throw new BeanCreationException("Beans annotated with @Bean can not have parameters on instance() method");
                }
                instance = method;
                requiredType = method.getReturnType();
            } catch (NoSuchMethodException e) {
                throw new BeanCreationException("Beans annotated with @Bean need to have instance() method");
            }
        } else {
            if (isSingleton) {
                instance = classInstance;
            }
        }
        createBeanDefinition(beanName, isSingleton ? BeanScope.SINGLETON : BeanScope.PROTOTYPE,
                requiredType, clazz, instance);
    }

    @Override
    public void registerSingletonBeanDefinition(String beanName, Class<?> clazz, Object instance) {
        if (containsBeanDefinition(beanName, clazz)) {
            throw new BeanCreationException("A bean with name %s of type %s has already been registered");
        }
        createBeanDefinition(beanName, BeanScope.SINGLETON, clazz, clazz, instance);
    }

    @Override
    public Set<BeanDefinition> getBeanDefinitionsByType(Class<?> requiredType) {
        return getBeanDefinitions().stream()
                .filter(beanDefinition -> beanDefinition.getType().equals(requiredType) || beanDefinition.getLiteralType().equals(requiredType))
                .collect(Collectors.toSet());
    }

    private void createBeanDefinition(String name, BeanScope scope, Class<?> type, Class<?> literalType, Object instance) {
        val beanDefinition = BeanDefinition.builder()
                .name(name)
                .scope(scope)
                .type(type)
                .literalType(literalType)
                .instance(instance)
                .build();
        beanDefinitions.add(beanDefinition);
    }
}