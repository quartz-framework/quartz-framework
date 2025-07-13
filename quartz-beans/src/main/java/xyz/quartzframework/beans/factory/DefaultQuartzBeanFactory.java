package xyz.quartzframework.beans.factory;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.definition.QuartzBeanDefinition;
import xyz.quartzframework.beans.definition.QuartzBeanDefinitionRegistry;
import xyz.quartzframework.beans.strategy.BeanNameStrategy;
import xyz.quartzframework.beans.support.BeanInjector;
import xyz.quartzframework.beans.support.BeanProvider;

import java.lang.annotation.Annotation;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

@NoProxy
@RequiredArgsConstructor
public class DefaultQuartzBeanFactory implements QuartzBeanFactory {

    private static final ThreadLocal<Set<UUID>> currentlyConstructing = ThreadLocal.withInitial(HashSet::new);

    private static final ThreadLocal<Deque<QuartzBeanDefinition>> constructionStack =
            ThreadLocal.withInitial(ArrayDeque::new);


    @Getter
    private final URLClassLoader classLoader;

    @Getter
    private final QuartzBeanDefinitionRegistry registry;

    private final BeanNameStrategy beanNameStrategy;

    @PreDestroy
    public void onDestroy() {
        currentlyConstructing.remove();
        constructionStack.remove();
    }

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
    public <T> BeanProvider<T> getBeanProvider(@NonNull Class<T> requiredType) {
        return BeanProvider.of(this, requiredType);
    }

    @NonNull
    @Override
    public <T> BeanProvider<T> getBeanProvider(@NonNull ResolvableType requiredType) {
        return BeanProvider.of(this, requiredType);
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
        return registry
                .getBeanDefinitions()
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
                .map(def -> def.getTypeMetadata().getType())
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
                .map(QuartzBeanDefinition::getName)
                .toArray(String[]::new);
    }

    @NonNull
    @Override
    public <T> BeanProvider<T> getBeanProvider(@NonNull Class<T> requiredType, boolean allowEagerInit) {
        return getBeanProvider(requiredType);
    }

    @NonNull
    @Override
    public <T> BeanProvider<T> getBeanProvider(@NonNull ResolvableType requiredType, boolean allowEagerInit) {
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
                        QuartzBeanDefinition::getName,
                        def -> getBean(def.getName(), type),
                        (a, b) -> a
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
                .filter(def -> def.getTypeMetadata().hasAnnotation(annotationType))
                .map(QuartzBeanDefinition::getName)
                .toArray(String[]::new);
    }

    @NonNull
    @Override
    public Map<String, Object> getBeansWithAnnotation(@NonNull Class<? extends Annotation> annotationType) throws BeansException {
        return registry
                .getBeanDefinitions()
                .stream()
                .filter(def -> def.getTypeMetadata().hasAnnotation(annotationType))
                .collect(Collectors.toMap(
                        QuartzBeanDefinition::getName,
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
        return def.getTypeMetadata()
                .getAnnotation(annotationType)
                .map(meta -> meta.toReflectiveAnnotation(classLoader, annotationType))
                .orElse(null);
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(@NonNull String beanName, @NonNull Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        return findAnnotationOnBean(beanName, annotationType);
    }

    @NonNull

    @Override
    public <A extends Annotation> Set<A> findAllAnnotationsOnBean(@NonNull String beanName, @NonNull Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        val def = registry
                .getBeanDefinitions()
                .stream()
                .filter(d -> d.getName().equals(beanName))
                .findFirst()
                .orElseThrow(() -> new NoSuchBeanDefinitionException(beanName));
        Set<A> annotations = new HashSet<>();
        for (val annotation : def.getTypeMetadata().getAnnotations()) {
            try {
                Class<?> annType = Class.forName(annotation.getName(), false, classLoader);
                if (annotationType.isAssignableFrom(annType)) {
                    A resolved = annotation.toReflectiveAnnotation(classLoader, annotationType);
                    if (resolved != null) annotations.add(resolved);
                }
            } catch (ClassNotFoundException ignored) {}
        }
        return annotations;
    }

    @NonNull
    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        if (type == null) return new String[0];
        return registry.getBeanDefinitionsByType(type)
                .stream()
                .map(QuartzBeanDefinition::getName)
                .toArray(String[]::new);
    }

    @NonNull
    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return getBeanNamesForType(type);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T getInstance(QuartzBeanDefinition beanDefinition) {
        val id = beanDefinition.getId();
        if (!currentlyConstructing.get().add(id)) {
            throw new BeanCreationException("Recursive instantiation detected for bean: " + id);
        }
        val stack = constructionStack.get();
        if (stack.contains(beanDefinition)) {
            val cycle = stack.stream()
                    .map(b -> b.getName() + "(" + b.getTypeMetadata().getSimpleName() + ")")
                    .collect(Collectors.joining(" -> "));
            throw new BeanCreationException("Circular dependency detected: " + cycle + " -> " + beanDefinition.getName());
        }
        stack.push(beanDefinition);
        try {
            if (beanDefinition.isSingleton() && beanDefinition.getInstance() != null) {
                return (T) beanDefinition.getInstance();
            }
            val instance = (T) createInstance(beanDefinition);
            BeanInjector.recursiveInjection(this, instance);
            if (beanDefinition.isSingleton()) {
                registry.updateBeanInstance(beanDefinition, instance);
            }
            if (beanDefinition.isDeferred()) {
                beanDefinition.construct(this);
            }
            return instance;
        } finally {
            currentlyConstructing.get().remove(id);
            stack.pop();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(QuartzBeanDefinition beanDefinition) {
        val metadata = beanDefinition.getTypeMetadata();
        T instance;
        if (beanDefinition.isInternalBean()) {
            val type = metadata.getType();
            instance = (T) BeanInjector.newInstance(this, type);
        } else {
            val matchingMethod = beanDefinition.getProvideMethods().get(0).getMethod();
            if (matchingMethod == null) {
                throw new BeanCreationException("Cannot create instance of " + beanDefinition.getName() + " — no matching @Provide method found");
            }
            instance = BeanInjector.newInstance(this, matchingMethod);
        }
        if (shouldProxyWithAspect(beanDefinition)) {
            return aspectInstance(instance, beanDefinition.isSingleton());
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
                .filter(QuartzBeanDefinition::isAspect)
                .map(QuartzBeanDefinition::getInstance)
                .filter(Objects::nonNull)
                .forEach(factory::addAspect);
        return factory.getProxy(getClassLoader());
    }

    private boolean shouldProxyWithAspect(QuartzBeanDefinition beanDefinition) {
        if (beanDefinition.isAspect()) {
            return false;
        }
        val matches = getRegistry()
                .getBeanDefinitions()
                .stream()
                .filter(QuartzBeanDefinition::isAspect)
                .map(QuartzBeanDefinition::getInstance)
                .anyMatch(Objects::nonNull);
        return beanDefinition.isProxied() && matches;
    }
}