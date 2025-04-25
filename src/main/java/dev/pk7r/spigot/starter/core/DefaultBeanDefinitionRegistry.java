package dev.pk7r.spigot.starter.core;

import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.exception.BeanCreationException;
import dev.pk7r.spigot.starter.core.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.core.model.BeanDefinition;
import dev.pk7r.spigot.starter.core.model.BeanScope;
import dev.pk7r.spigot.starter.core.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.strategy.DefaultBeanNameStrategy;
import dev.pk7r.spigot.starter.core.util.BeanUtil;
import dev.pk7r.spigot.starter.core.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pacesys.reflect.Reflect;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Getter
@AllArgsConstructor
class DefaultBeanDefinitionRegistry implements BeanDefinitionRegistry {

    DefaultPluginContext pluginContext;

    private final Set<BeanDefinition> beanDefinitions = new HashSet<>();

    @Override
    public BeanDefinition getBeanDefinition(Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .min((a, b) -> Boolean.compare(!a.isPrimary(), !b.isPrimary()))
                .orElseThrow(() -> new BeanNotFoundException("No beans found for " + requiredType.getSimpleName()));
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName, Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .filter(beanDefinition -> beanDefinition.getName().equals(beanName))
                .min((a, b) -> Boolean.compare(!a.isPrimary(), !b.isPrimary()))
                .orElseThrow(() -> new BeanNotFoundException("No beans found for " + requiredType.getSimpleName()));
    }

    @Override
    public boolean containsBeanDefinition(String beanName, Class<?> requiredType) {
        return beanDefinitions.stream()
                .filter(filterBeanDefinition(requiredType))
                .anyMatch(beanDefinition -> beanDefinition.getName().equals(beanName));
    }

    @Override
    @SneakyThrows
    public void registerBeanDefinition(Class<?> clazz) {
        if (clazz.isInterface()) {
            throw new BeanCreationException("Bean type can't be an interface");
        }
        String beanName = null;
        val hasNamedInstance = BeanUtil.hasNamedInstance(clazz);
        if (hasNamedInstance) {
            beanName = BeanUtil.getNamedInstance(clazz);
        }
        if (Objects.isNull(beanName) || beanName.isEmpty()) {
            beanName = DefaultBeanNameStrategy.getInstance().generateBeanName(clazz);
        }
        Class<?> requiredType;
        if (clazz.getInterfaces().length != 0) {
            requiredType = clazz.getInterfaces()[0];
        } else if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            requiredType = clazz.getSuperclass();
        } else {
            requiredType = clazz;
        }
        if (containsBeanDefinition(beanName, requiredType)) {
            throw new BeanCreationException("A bean with name %s of type %s has already been registered");
        }
        val primary = BeanUtil.isPrimary(clazz);
        val isSingleton = BeanUtil.isSingleton(clazz);
        val isLazy = BeanUtil.isLazy(clazz);
        val beanFactory = pluginContext.getBeanFactory();
        val environmentPostProcessor = pluginContext.getPropertyPostProcessor();
        Object instance = isLazy ? null : BeanUtil.newInstance(beanFactory, clazz);
        createBeanDefinition(beanName, primary, true, hasNamedInstance, isSingleton ? BeanScope.SINGLETON : BeanScope.PROTOTYPE,
                requiredType, clazz, instance);
        val methods = ReflectionUtil.getMethods(Reflect.MethodType.ALL, clazz, Bean.class);
        methods.forEach(method -> {
            if (!BeanUtil.isBean(method)) return;
            val primaryBean = BeanUtil.isPrimary(method);
            val isSingletonBean = BeanUtil.isSingleton(method);
            String methodBeanName = null;
            val hasMethodNamedInstance = BeanUtil.hasNamedInstance(method);
            if (hasMethodNamedInstance) {
                methodBeanName = BeanUtil.getNamedInstance(method);
            }
            if (Objects.isNull(methodBeanName) || methodBeanName.isEmpty()) {
                methodBeanName = DefaultBeanNameStrategy.getInstance().generateBeanName(method);
            }
            val isLazyMethod = BeanUtil.isLazy(method);
            Object methodInstance = isLazyMethod ? null : isLazy ? null : BeanUtil.newInstance(beanFactory, method);
            createBeanDefinition(methodBeanName, primaryBean, false, hasNamedInstance, isSingletonBean ? BeanScope.SINGLETON : BeanScope.PROTOTYPE,
                    method.getReturnType(), clazz, methodInstance);
        });
    }

    @Override
    public void registerSingletonBeanDefinition(Object instance) {
        val aClass = instance.getClass();
        registerSingletonBeanDefinition(aClass, instance);
    }

    @Override
    public void registerSingletonBeanDefinition(Class<?> clazz, Object instance) {
        registerSingletonBeanDefinition(DefaultBeanNameStrategy.getInstance().generateBeanName(clazz), clazz, instance);
    }

    @Override
    public void registerSingletonBeanDefinition(String beanName, Class<?> clazz, Object instance) {
        if (containsBeanDefinition(beanName, clazz)) {
            throw new BeanCreationException("A bean with name %s of type %s has already been registered");
        }
        createBeanDefinition(beanName, false,true, false, BeanScope.SINGLETON, clazz, clazz, instance);
    }

    @Override
    public Set<BeanDefinition> getBeanDefinitionsByType(Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .collect(Collectors.toSet());
    }

    private void createBeanDefinition(String name, boolean primary, boolean internalBean, boolean hasNamedInstance, BeanScope scope, Class<?> type, Class<?> literalType, Object instance) {
        val beanDefinition = BeanDefinition.builder()
                .name(name)
                .scope(scope)
                .type(type)
                .primary(primary)
                .namedInstance(hasNamedInstance)
                .internalBean(internalBean)
                .literalType(literalType)
                .instance(instance)
                .build();
        val pluginMain = pluginContext.getPluginApplication();
        if (pluginMain.verbose()) {
            log.info("Registering bean definition: {}", beanDefinition);
        }
        beanDefinitions.add(beanDefinition);
    }

    @Override
    public <T> T updateBeanInstance(BeanDefinition beanDefinition, T instance) {
        beanDefinition.setInstance(instance);
        beanDefinitions.removeIf(b -> b.getId().equals(beanDefinition.getId()));
        beanDefinitions.add(beanDefinition);
        return instance;
    }

    private Predicate<BeanDefinition> filterBeanDefinition(Class<?> requiredType) {
        return beanDefinition -> requiredType.isAssignableFrom(beanDefinition.getType()) ||
                requiredType.isAssignableFrom(beanDefinition.getLiteralType()) ||
                (beanDefinition.getInstance() != null && requiredType.isAssignableFrom(beanDefinition.getInstance().getClass()));
    }
}