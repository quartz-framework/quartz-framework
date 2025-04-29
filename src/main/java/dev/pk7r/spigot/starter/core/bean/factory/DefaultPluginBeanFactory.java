package dev.pk7r.spigot.starter.core.bean.factory;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.bean.PluginBeanDefinition;
import dev.pk7r.spigot.starter.core.bean.registry.PluginBeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.bean.strategy.BeanNameStrategy;
import dev.pk7r.spigot.starter.core.exception.BeanCreationException;
import dev.pk7r.spigot.starter.core.util.BeanUtil;
import dev.pk7r.spigot.starter.core.util.InjectionUtil;
import dev.pk7r.spigot.starter.core.util.ReflectionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pacesys.reflect.Reflect;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@NoProxy
@RequiredArgsConstructor
public class DefaultPluginBeanFactory implements PluginBeanFactory {

    @Getter
    private final URLClassLoader classLoader;

    @Getter
    private final PluginBeanDefinitionRegistry registry;

    private final BeanNameStrategy beanNameStrategy;

    @NonNull
    @Override
    public Object getBean(@NonNull String name) throws BeansException {
        val definition = registry
                .getBeanDefinitions()
                .stream()
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchBeanDefinitionException(name));
        return getInstance(definition);
    }

    @NonNull
    @Override
    @SneakyThrows
    public <T> T getBean(@NonNull Class<T> requiredType) throws BeansException {
        val definition = registry.getBeanDefinition(requiredType);
        return getInstance(definition);
    }

    @NonNull
    @Override
    public <T> T getBean(@NonNull Class<T> requiredType, @NonNull Object... args) throws BeansException {
        return getBean(requiredType);
    }

    @NonNull
    @Override
    public <T> DefaultObjectProvider<T> getBeanProvider(@NonNull Class<T> requiredType) {
        return () -> getBean(requiredType);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> DefaultObjectProvider<T> getBeanProvider(@NonNull ResolvableType requiredType) {
        return () -> (T) getBeanProvider(requiredType);
    }

    @NonNull
    @Override
    @SneakyThrows
    public <T> T getBean(@NonNull String name, @NonNull Class<T> requiredType) throws BeansException {
        val definition = registry.getBeanDefinition(name, requiredType);
        return getInstance(definition);
    }

    @NonNull
    @Override
    public Object getBean(@NonNull String name, @NonNull Object... args) throws BeansException {
        return getBean(name);
    }

    @Override
    public boolean containsBean(@NonNull String name) {
        return registry.getBeanDefinitions()
                .stream()
                .anyMatch(d -> d.getName().equals(name));
    }

    @Override
    public boolean isSingleton(@NonNull String name) throws NoSuchBeanDefinitionException {
        return getRegistry().getBeanDefinition(name).isSingleton();
    }

    @Override
    public boolean isPrototype(@NonNull String name) throws NoSuchBeanDefinitionException {
        return getRegistry().getBeanDefinition(name).isPrototype();
    }

    @Override
    public boolean isTypeMatch(@NonNull String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        if (typeToMatch.getRawClass() == null) return false;
        return isTypeMatch(name, typeToMatch.getRawClass());
    }

    @Override
    public boolean isTypeMatch(@NonNull String name, @NonNull Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        Class<?> beanType = getType(name);
        if (beanType == null) return false;
        return typeToMatch.isAssignableFrom(beanType);
    }

    @Override
    public Class<?> getType(@NonNull String name) throws NoSuchBeanDefinitionException {
        return registry
                .getBeanDefinitions()
                .stream()
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .map(PluginBeanDefinition::getType)
                .orElseThrow(() -> new NoSuchBeanDefinitionException(name));
    }

    @Override
    public Class<?> getType(@NonNull String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        return getType(name);
    }

    @NonNull
    @Override
    public String[] getAliases(@NonNull String name) {
        return Arrays
                .stream(getBeanDefinitionNames())
                .filter(n -> n.equals(name))
                .toArray(String[]::new);
    }

    @Override
    public boolean containsBeanDefinition(@NonNull String beanName) {
        return registry.getBeanDefinitions()
                .stream()
                .anyMatch(d -> d.getName().equals(beanName));
    }

    @Override
    public int getBeanDefinitionCount() {
        return registry.getBeanDefinitions().size();
    }

    @NonNull
    @Override
    public String[] getBeanDefinitionNames() {
        return registry.getBeanDefinitions()
                .stream()
                .map(PluginBeanDefinition::getName)
                .toArray(String[]::new);
    }

    @NonNull
    @Override
    public <T> DefaultObjectProvider<T> getBeanProvider(@NonNull Class<T> requiredType, boolean allowEagerInit) {
        return getBeanProvider(requiredType);
    }

    @NonNull
    @Override
    public <T> DefaultObjectProvider<T> getBeanProvider(@NonNull ResolvableType requiredType, boolean allowEagerInit) {
        return getBeanProvider(requiredType);
    }

    @NonNull
    @Override
    public String[] getBeanNamesForType(ResolvableType type) {
        return getBeanNamesForType(type.getRawClass());
    }

    @NonNull
    @Override
    public String[] getBeanNamesForType(@NonNull ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        return getBeanNamesForType(type);
    }

    @NonNull
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return registry
                .getBeanDefinitionsByType(type)
                .stream()
                .collect(Collectors.toMap(
                        PluginBeanDefinition::getName,
                        def -> (T) getBean(def.getName(), type)
                ));
    }

    @NonNull
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        return getBeansOfType(type);
    }

    @NonNull
    @Override
    public String[] getBeanNamesForAnnotation(@NonNull Class<? extends Annotation> annotationType) {
        return registry
                .getBeanDefinitions()
                .stream()
                .filter(def -> def.getType().isAnnotationPresent(annotationType)
                || def.getLiteralType().isAnnotationPresent(annotationType))
                .map(PluginBeanDefinition::getName)
                .toArray(String[]::new);
    }

    @NonNull
    @Override
    public Map<String, Object> getBeansWithAnnotation(@NonNull Class<? extends Annotation> annotationType) throws BeansException {
        return registry
                .getBeanDefinitions()
                .stream()
                .filter(def -> def.getType().isAnnotationPresent(annotationType)
                        || def.getLiteralType().isAnnotationPresent(annotationType))
                .collect(Collectors.toMap(
                        PluginBeanDefinition::getName,
                        def -> getBean(def.getName())
                ));
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(@NonNull String beanName, @NonNull Class<A> annotationType) throws NoSuchBeanDefinitionException {
        val def = registry
                .getBeanDefinitions()
                .stream()
                .filter(d -> d.getName().equals(beanName))
                .findFirst()
                .orElseThrow(() -> new NoSuchBeanDefinitionException(beanName));
        return def.getType().getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(@NonNull String beanName, @NonNull Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        return findAnnotationOnBean(beanName, annotationType);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> Set<A> findAllAnnotationsOnBean(@NonNull String beanName, @NonNull Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        val def = registry
                .getBeanDefinitions()
                .stream()
                .filter(d -> d.getName().equals(beanName))
                .findFirst()
                .orElseThrow(() -> new NoSuchBeanDefinitionException(beanName));
        Set<A> annotations = new HashSet<>();
        for (Annotation ann : def.getType().getAnnotations()) {
            if (annotationType.isAssignableFrom(ann.annotationType())) {
                annotations.add((A) ann);
            }
        }
        return annotations;
    }

    @NonNull
    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        if (type == null) return new String[0];
        return registry.getBeanDefinitionsByType(type)
                .stream()
                .map(PluginBeanDefinition::getName)
                .toArray(String[]::new);
    }

    @NonNull
    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return getBeanNamesForType(type);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T getInstance(PluginBeanDefinition pluginBeanDefinition) {
        if (pluginBeanDefinition.isSingleton() && pluginBeanDefinition.getInstance() != null) {
            return (T) pluginBeanDefinition.getInstance();
        }
        val instance = (T) createInstance(pluginBeanDefinition);
        InjectionUtil.recursiveInjection(this, instance);
        if (pluginBeanDefinition.isSingleton()) {
            registry.updateBeanInstance(pluginBeanDefinition, instance);
        }
        if (pluginBeanDefinition.isDeferred() || pluginBeanDefinition.isPrototype()) {
            pluginBeanDefinition.construct(this);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(PluginBeanDefinition pluginBeanDefinition) {
        Class<?> literalType = pluginBeanDefinition.getLiteralType();
        T instance;
        if (pluginBeanDefinition.isInternalBean()) {
            instance = (T) InjectionUtil.newInstance(this, literalType);
        } else {
            val methods = ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, literalType, Provide.class);
            val method = methods
                    .stream()
                    .filter(k -> {
                        val nameCandidate = beanNameStrategy.generateBeanName(k);
                        if (BeanUtil.hasNamedInstance(k)) {
                            return nameCandidate.equals(BeanUtil.getNamedInstance(k));
                        } else {
                            return pluginBeanDefinition.getName().equals(nameCandidate);
                        }
                    })
                    .findFirst()
                    .orElse(null);
            if (method != null) {
                instance = InjectionUtil.newInstance(this, method);
            } else {
                throw new BeanCreationException("Cannot create instance of " + pluginBeanDefinition.getName());
            }
        }
        if (shouldProxyWithAspect(pluginBeanDefinition)) {
            return aspectInstance(instance, pluginBeanDefinition.isSingleton());
        }
        return instance;
    }

    private <T> T aspectInstance(T instance, boolean singleton) {
        if (!singleton) {
            return instance;
        }
        val factory = new AspectJProxyFactory(instance);
        factory.setProxyTargetClass(true);
        getRegistry()
                .getBeanDefinitions()
                .stream()
                .filter(PluginBeanDefinition::isAspect)
                .map(PluginBeanDefinition::getInstance)
                .filter(Objects::nonNull)
                .forEach(factory::addAspect);
        return factory.getProxy(getClassLoader());
    }

    private boolean shouldProxyWithAspect(PluginBeanDefinition pluginBeanDefinition) {
        if (pluginBeanDefinition.isAspect()) {
            return false;
        }
        val matches = getRegistry()
                .getBeanDefinitions()
                .stream()
                .filter(PluginBeanDefinition::isAspect)
                .map(PluginBeanDefinition::getInstance)
                .anyMatch(Objects::nonNull);
        return pluginBeanDefinition.isProxied() && matches;
    }

    @FunctionalInterface
    public interface DefaultObjectProvider<T> extends ObjectProvider<T>, Supplier<T> {

        @NonNull
        @Override
        default T getObject() throws BeansException {
            return get();
        }

        @NonNull
        @Override
        default T getObject(@NonNull Object... args) throws BeansException {
            return get();
        }

        @Override
        default T getIfAvailable() throws BeansException {
            return get();
        }

        @Override
        default T getIfUnique() throws BeansException {
            return get();
        }
    }
}