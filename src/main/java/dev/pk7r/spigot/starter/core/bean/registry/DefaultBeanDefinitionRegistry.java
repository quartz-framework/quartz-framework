package dev.pk7r.spigot.starter.core.bean.registry;

import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.RepeatedTask;
import dev.pk7r.spigot.starter.core.annotation.condition.*;
import dev.pk7r.spigot.starter.core.bean.BeanDefinition;
import dev.pk7r.spigot.starter.core.bean.BeanScope;
import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.bean.strategy.BeanNameStrategy;
import dev.pk7r.spigot.starter.core.condition.*;
import dev.pk7r.spigot.starter.core.exception.BeanCreationException;
import dev.pk7r.spigot.starter.core.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.core.exception.LifecycleException;
import dev.pk7r.spigot.starter.core.util.BeanUtil;
import dev.pk7r.spigot.starter.core.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pacesys.reflect.Reflect;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Getter
@AllArgsConstructor
public class DefaultBeanDefinitionRegistry implements BeanDefinitionRegistry {

    private final BeanNameStrategy beanNameStrategy;

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
    public List<BeanDefinition> registerBeanDefinition(BeanFactory beanFactory, Class<?> clazz) {
        val definitions = new ArrayList<BeanDefinition>();
        if (clazz.isInterface()) {
            throw new BeanCreationException("Bean type can't be an interface");
        }
        String beanName = null;
        val hasNamedInstance = BeanUtil.hasNamedInstance(clazz);
        if (hasNamedInstance) {
            beanName = BeanUtil.getNamedInstance(clazz);
        }
        if (Objects.isNull(beanName) || beanName.isEmpty()) {
            beanName = beanNameStrategy.generateBeanName(clazz);
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
        val methods = ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, clazz, PostConstruct.class, PreDestroy.class, RepeatedTask.class);
        val lifecycleMethods = new HashMap<Class<? extends Annotation>, List<Method>>();
        methods.forEach(method -> {
            if (BeanUtil.isBean(method)) return;
            boolean isLifecycleMethod =
                    method.isAnnotationPresent(PostConstruct.class) ||
                            method.isAnnotationPresent(PreDestroy.class) ||
                            method.isAnnotationPresent(RepeatedTask.class);
            if (!isLifecycleMethod) return;
            if (method.getParameterCount() > 0) {
                throw new LifecycleException("Lifecycle methods cannot have parameters");
            }
            if (!method.getReturnType().equals(Void.TYPE)) {
                throw new LifecycleException("Lifecycle methods must return void");
            }
            if (method.isAnnotationPresent(PostConstruct.class)) {
                lifecycleMethods.computeIfAbsent(PostConstruct.class, k -> new ArrayList<>()).add(method);
            }
            if (method.isAnnotationPresent(RepeatedTask.class)) {
                lifecycleMethods.computeIfAbsent(RepeatedTask.class, k -> new ArrayList<>()).add(method);
            }
            if (method.isAnnotationPresent(PreDestroy.class)) {
                lifecycleMethods.computeIfAbsent(PreDestroy.class, k -> new ArrayList<>()).add(method);
            }
        });
        val parent = createBeanDefinition(beanName, primary, isLazy, true, hasNamedInstance, BeanUtil.isAutoConfiguration(clazz), isSingleton ? BeanScope.SINGLETON : BeanScope.PROTOTYPE,
                requiredType, clazz, null,
                clazz.getAnnotation(ConditionalOnBean.class),
                clazz.getAnnotation(ConditionalOnValue.class),
                clazz.getAnnotation(ConditionalOnMissingBean.class),
                clazz.getAnnotation(ConditionalOnAnnotation.class),
                clazz.getAnnotation(ConditionalOnClass.class),
                clazz.getAnnotation(ConditionalOnMissingClass.class),
                lifecycleMethods.getOrDefault(PostConstruct.class, new ArrayList<>()),
                lifecycleMethods.getOrDefault(PreDestroy.class, new ArrayList<>()),
                lifecycleMethods.getOrDefault(RepeatedTask.class, new ArrayList<>()));
        definitions.add(parent);
        val beans = ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, clazz, Bean.class);
        beans.forEach(method -> {
            val annotation = method.getAnnotation(Bean.class);
            if (annotation == null) return;
            val beanPostConstructMethods = methods
                    .stream()
                    .filter(m -> Arrays.stream(annotation.postConstructMethods()).anyMatch(s -> s.equals(method.getName())))
                    .collect(Collectors.toList());
            val beanPreDestroyMethods = methods
                    .stream()
                    .filter(m -> Arrays.stream(annotation.preDestroyMethods()).anyMatch(s -> s.equals(method.getName())))
                    .collect(Collectors.toList());
            val primaryBean = BeanUtil.isPrimary(method);
            val isSingletonBean = BeanUtil.isSingleton(method);
            String methodBeanName = null;
            val hasMethodNamedInstance = BeanUtil.hasNamedInstance(method);
            if (hasMethodNamedInstance) {
                methodBeanName = BeanUtil.getNamedInstance(method);
            }
            if (Objects.isNull(methodBeanName) || methodBeanName.isEmpty()) {
                methodBeanName = beanNameStrategy.generateBeanName(method);
            }
            val isLazyMethod = BeanUtil.isLazy(method);
            val bean = createBeanDefinition(methodBeanName,
                    primaryBean,
                    isLazyMethod,
                    false,
                    hasNamedInstance,
                    false,
                    isSingletonBean ? BeanScope.SINGLETON : BeanScope.PROTOTYPE,
                    method.getReturnType(),
                    clazz,
                    null,
                    method.getAnnotation(ConditionalOnBean.class),
                    method.getAnnotation(ConditionalOnValue.class),
                    method.getAnnotation(ConditionalOnMissingBean.class),
                    method.getAnnotation(ConditionalOnAnnotation.class),
                    method.getAnnotation(ConditionalOnClass.class),
                    method.getAnnotation(ConditionalOnMissingClass.class),
                    beanPostConstructMethods,
                    beanPreDestroyMethods,
                    Collections.emptyList());
            definitions.add(bean);
        });
        return definitions;
    }

    @Override
    public void unregisterBeanDefinition(UUID id) {
        getBeanDefinitions().removeIf(beanDefinition -> {
            if (beanDefinition.getId().equals(id)) {
                beanDefinition.destroy();
                return true;
            }
            return false;
        });
    }

    @Override
    public void registerSingletonBeanDefinition(Object instance) {
        val aClass = instance.getClass();
        registerSingletonBeanDefinition(aClass, instance);
    }

    @Override
    public void registerSingletonBeanDefinition(Class<?> clazz, Object instance) {
        registerSingletonBeanDefinition(beanNameStrategy.generateBeanName(clazz), clazz, instance);
    }

    @Override
    public void registerSingletonBeanDefinition(String beanName, Class<?> clazz, Object instance) {
        if (containsBeanDefinition(beanName, clazz)) {
            throw new BeanCreationException("A bean with name %s of type %s has already been registered");
        }
        createBeanDefinition(beanName, false, false,true, false, BeanUtil.isAutoConfiguration(clazz), BeanScope.SINGLETON, clazz, clazz, instance, null, null, null, null, null,null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public Set<BeanDefinition> getBeanDefinitionsByType(Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .collect(Collectors.toSet());
    }

    private BeanDefinition createBeanDefinition(String name,
                                                boolean primary,
                                                boolean lazy,
                                                boolean internalBean,
                                                boolean hasNamedInstance,
                                                boolean autoConfiguration,
                                                BeanScope scope, Class<?> type, Class<?> literalType,
                                                Object instance,
                                                ConditionalOnBean conditionalOnBean,
                                                ConditionalOnValue conditionalOnValue,
                                                ConditionalOnMissingBean conditionalOnMissingBean,
                                                ConditionalOnAnnotation conditionalOnAnnotation,
                                                ConditionalOnClass conditionalOnClass,
                                                ConditionalOnMissingClass conditionalOnMissingClass,
                                                List<Method> postConstructMethods,
                                                List<Method> preDestroyMethods,
                                                List<Method> repeatedTasksMethods) {
        val beanDefinition = BeanDefinition.builder()
                .name(name)
                .scope(scope)
                .type(type)
                .primary(primary)
                .namedInstance(hasNamedInstance)
                .autoConfiguration(autoConfiguration)
                .internalBean(internalBean)
                .lazy(lazy)
                .conditionalOnBean(ConditionalOnBeanMetadata.of(conditionalOnBean))
                .conditionalOnMissingClass(ConditionalOnMissingClassMetadata.of(conditionalOnMissingClass))
                .conditionalOnValue(ConditionalOnValueMetadata.of(conditionalOnValue))
                .conditionalOnMissingBean(ConditionalOnMissingBeanMetadata.of(conditionalOnMissingBean))
                .conditionalOnAnnotation(ConditionalOnAnnotationMetadata.of(conditionalOnAnnotation))
                .conditionalOnClass(ConditionalOnClassMetadata.of(conditionalOnClass))
                .postConstructMethods(postConstructMethods)
                .preDestroyMethods(preDestroyMethods)
                .repeatedTasksMethods(repeatedTasksMethods)
                .literalType(literalType)
                .instance(instance)
                .build();
        beanDefinitions.add(beanDefinition);
        return beanDefinition;
    }

    @Override
    public <T> T updateBeanInstance(BeanDefinition beanDefinition, T instance) {
        beanDefinition.setInstance(instance);
        return instance;
    }

    @Override
    public Predicate<BeanDefinition> filterBeanDefinition(Class<?> requiredType) {
        return beanDefinition -> {
            if (!beanDefinition.isInternalBean()) {
                return requiredType.isAssignableFrom(beanDefinition.getType());
            }
            return requiredType.isAssignableFrom(beanDefinition.getType()) ||
                    requiredType.isAssignableFrom(beanDefinition.getLiteralType());
        };
    }
}