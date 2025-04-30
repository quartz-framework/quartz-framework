package xyz.quartzframework.core.bean.registry;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.pacesys.reflect.Reflect;
import org.springframework.beans.factory.config.BeanDefinition;
import xyz.quartzframework.core.annotation.Listen;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.bean.PluginBeanDefinition;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.condition.annotation.*;
import xyz.quartzframework.core.condition.metadata.*;
import xyz.quartzframework.core.exception.BeanCreationException;
import xyz.quartzframework.core.exception.BeanNotFoundException;
import xyz.quartzframework.core.exception.LifecycleException;
import xyz.quartzframework.core.task.RepeatedTask;
import xyz.quartzframework.core.util.BeanUtil;
import xyz.quartzframework.core.util.ReflectionUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@NoProxy
@Slf4j
@Getter
@AllArgsConstructor
public class DefaultPluginBeanDefinitionRegistry implements PluginBeanDefinitionRegistry {

    private final BeanNameStrategy beanNameStrategy;

    private final Set<PluginBeanDefinition> beanDefinitions = new HashSet<>();

    @NonNull
    @Override
    public PluginBeanDefinition getBeanDefinition(@NonNull String beanName) {
        return getBeanDefinitions()
                .stream()
                .filter(b -> b.getName().equals(beanName))
                .min((a, b) -> Boolean.compare(!a.isPreferred(), !b.isPreferred()))
                .orElseThrow(() -> new BeanNotFoundException("No beans found for " + beanName));
    }

    @Override
    public boolean containsBeanDefinition(@NonNull String beanName) {
        return getBeanDefinitions()
                .stream()
                .anyMatch(b -> b.getName().equals(beanName));
    }

    @NonNull
    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanDefinitions()
                .stream()
                .map(PluginBeanDefinition::getName)
                .toArray(String[]::new);
    }

    @Override
    public int getBeanDefinitionCount() {
        return getBeanDefinitions().size();
    }

    @Override
    public boolean isBeanNameInUse(@NonNull String beanName) {
        return containsBeanDefinition(beanName);
    }

    @Override
    public PluginBeanDefinition getBeanDefinition(Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .min((a, b) -> Boolean.compare(!a.isPreferred(), !b.isPreferred()))
                .orElseThrow(() -> new BeanNotFoundException("No beans found for " + requiredType.getSimpleName()));
    }

    @Override
    public PluginBeanDefinition getBeanDefinition(String beanName, Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .filter(b -> b.getName().equals(beanName))
                .findFirst()
                .orElseGet(() -> getBeanDefinition(requiredType));
    }

    @Override
    public boolean containsBeanDefinition(String beanName, Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .anyMatch(b -> b.getName().equals(beanName));
    }

    @Override
    @SneakyThrows
    public void defineBeans(Class<?> clazz) {
        if (clazz.isInterface()) throw new BeanCreationException("Bean type can't be an interface");
        val isProxy = BeanUtil.isProxy(clazz);
        val isAspect = BeanUtil.isAspect(clazz);
        val hasNamedInstance = BeanUtil.hasNamedInstance(clazz);
        val beanName = hasNamedInstance ? BeanUtil.getNamedInstance(clazz) : beanNameStrategy.generateBeanName(clazz);
        val requiredType = resolveType(clazz);
        if (containsBeanDefinition(beanName, requiredType)) {
            throw new BeanCreationException(String.format("A bean with name '%s' of type '%s' has already been registered", beanName, requiredType.getName()));
        }
        val lifecycleMethods = mapLifecycleMethods(clazz);
        val listenMethods = mapListenMethods(clazz);
        val definition = PluginBeanDefinition
                .builder()
                .internalBean(true)
                .name(beanName)
                .preferred(BeanUtil.isPreferred(clazz))
                .deferred(BeanUtil.isDeferred(clazz))
                .proxied(isProxy)
                .aspect(isAspect)
                .namedInstance(hasNamedInstance)
                .description(BeanUtil.getDescription(clazz))
                .contextBootstrapper(BeanUtil.isContextBootstrapper(clazz))
                .contextBootstrapper(BeanUtil.isBootstrapper(clazz))
                .configurer(BeanUtil.isConfigurer(clazz))
                .aspect(isAspect)
                .singleton(BeanUtil.isSingleton(clazz))
                .prototype(BeanUtil.isPrototype(clazz))
                .type(requiredType)
                .environments(BeanUtil.getEnvironments(clazz))
                .literalType(clazz)
                .order(BeanUtil.getOrder(clazz))
                .beanConditionMetadata(BeanConditionMetadata.of(clazz.getAnnotation(ActivateWhenBeanPresent.class)))
                .missingBeanConditionMetadata(BeanConditionMetadata.of(clazz.getAnnotation(ActivateWhenBeanMissing.class)))
                .classConditionMetadata(ClassConditionMetadata.of(clazz.getAnnotation(ActivateWhenClassPresent.class)))
                .missingClassConditionMetadata(ClassConditionMetadata.of(clazz.getAnnotation(ActivateWhenClassMissing.class)))
                .propertyConditionMetadata(PropertyConditionMetadata.of(clazz.getAnnotation(ActivateWhenPropertyEquals.class)))
                .annotationConditionMetadata(AnnotationConditionMetadata.of(clazz.getAnnotation(ActivateWhenAnnotationPresent.class)))
                .genericConditionMetadata(GenericConditionMetadata.of(clazz.getAnnotation(ActivateWhen.class)))
                .postConstructMethods(lifecycleMethods.getOrDefault(PostConstruct.class, Collections.emptyList()))
                .preDestroyMethods(lifecycleMethods.getOrDefault(PreDestroy.class, Collections.emptyList()))
                .repeatedTasksMethods(lifecycleMethods.getOrDefault(RepeatedTask.class, Collections.emptyList()))
                .listenMethods(listenMethods.getOrDefault(Listen.class, Collections.emptyList()))
                .build();
        registerBeanDefinition(definition.getName(), definition);
        defineProvideMethods(clazz, isProxy);
    }

    private void defineProvideMethods(Class<?> clazz, boolean parentIsProxy) {
        val methods = ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, clazz, Provide.class);
        for (val method : methods) {
            val beanAnnotation = method.getAnnotation(Provide.class);
            if (beanAnnotation == null) continue;
            val methodName = BeanUtil.hasNamedInstance(method)
                    ? BeanUtil.getNamedInstance(method)
                    : beanNameStrategy.generateBeanName(method);
            val definition = PluginBeanDefinition
                    .builder()
                    .name(methodName)
                    .preferred(BeanUtil.isPreferred(method))
                    .deferred(BeanUtil.isDeferred(method))
                    .contextBootstrapper(false)
                    .configurer(false)
                    .bootstrapper(false)
                    .internalBean(false)
                    .description(BeanUtil.getDescription(method))
                    .order(BeanUtil.getOrder(method))
                    .environments(BeanUtil.getEnvironments(method))
                    .proxied(parentIsProxy && BeanUtil.isProxy(method))
                    .namedInstance(BeanUtil.hasNamedInstance(method))
                    .singleton(BeanUtil.isSingleton(method))
                    .prototype(BeanUtil.isPrototype(method))
                    .type(method.getReturnType())
                    .literalType(clazz)
                    .aspect(BeanUtil.isAspect(method.getReturnType()))
                    .beanConditionMetadata(BeanConditionMetadata.of(method.getAnnotation(ActivateWhenBeanPresent.class)))
                    .missingBeanConditionMetadata(BeanConditionMetadata.of(method.getAnnotation(ActivateWhenBeanMissing.class)))
                    .classConditionMetadata(ClassConditionMetadata.of(method.getAnnotation(ActivateWhenClassPresent.class)))
                    .missingClassConditionMetadata(ClassConditionMetadata.of(method.getAnnotation(ActivateWhenClassMissing.class)))
                    .propertyConditionMetadata(PropertyConditionMetadata.of(method.getAnnotation(ActivateWhenPropertyEquals.class)))
                    .annotationConditionMetadata(AnnotationConditionMetadata.of(method.getAnnotation(ActivateWhenAnnotationPresent.class)))
                    .genericConditionMetadata(GenericConditionMetadata.of(method.getAnnotation(ActivateWhen.class)))
                    .build();
            registerBeanDefinition(definition.getName(), definition);
        }
    }

    private Map<Class<? extends Annotation>, List<Method>> mapLifecycleMethods(Class<?> clazz) {
        val methods = ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, clazz, PostConstruct.class, PreDestroy.class, RepeatedTask.class);
        val map = new HashMap<Class<? extends Annotation>, List<Method>>();
        for (val method : methods) {
            if (method.getParameterCount() > 0 || !method.getReturnType().equals(Void.TYPE)) {
                throw new LifecycleException("Lifecycle methods must have no parameters and return void");
            }
            for (val annotation : Arrays.asList(PostConstruct.class, PreDestroy.class, RepeatedTask.class)) {
                if (method.isAnnotationPresent(annotation)) {
                    map.computeIfAbsent(annotation, k -> new ArrayList<>()).add(method);
                }
            }
        }
        return map;
    }

    private Map<Class<? extends Annotation>, List<Method>> mapListenMethods(Class<?> clazz) {
        val methods = ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, clazz, Listen.class);
        val map = new HashMap<Class<? extends Annotation>, List<Method>>();
        for (val method : methods) {
            if (method.getParameterCount() != 1 || !method.getReturnType().equals(Void.TYPE)) {
                throw new LifecycleException("@Listen methods must have only the target event parameter and return void");
            }
            for (val annotation : Collections.singletonList(Listen.class)) {
                if (method.isAnnotationPresent(annotation)) {
                    map.computeIfAbsent(annotation, k -> new ArrayList<>()).add(method);
                }
            }
        }
        return map;
    }

    private Class<?> resolveType(Class<?> clazz) {
        Class<?> requiredType;
        if (clazz.getInterfaces().length != 0) {
            requiredType = clazz.getInterfaces()[0];
        } else if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            requiredType = clazz.getSuperclass();
        } else {
            requiredType = clazz;
        }
        return requiredType;
    }

    @Override
    public void unregisterBeanDefinition(UUID id) {
        getBeanDefinitions().removeIf(bean -> {
            if (bean.getId().equals(id)) {
                bean.destroy();
                return true;
            }
            return false;
        });
    }

    @Override
    public void removeBeanDefinition(@NonNull String beanName) {
        unregisterBeanDefinition(getBeanDefinition(beanName).getId());
    }

    @Override
    public void registerSingletonBeanDefinition(String beanName, Class<?> clazz, Object instance) {
        if (containsBeanDefinition(beanName, clazz)) {
            throw new BeanCreationException(String.format("A bean with name '%s' of type '%s' has already been registered", beanName, clazz.getName()));
        }
        val definition = PluginBeanDefinition
                .builder()
                .name(beanName)
                .preferred(BeanUtil.isPreferred(clazz))
                .deferred(BeanUtil.isDeferred(clazz))
                .proxied(BeanUtil.isProxy(clazz))
                .environments(BeanUtil.getEnvironments(clazz))
                .namedInstance(BeanUtil.hasNamedInstance(clazz))
                .contextBootstrapper(BeanUtil.isContextBootstrapper(clazz))
                .contextBootstrapper(BeanUtil.isBootstrapper(clazz))
                .configurer(BeanUtil.isConfigurer(clazz))
                .aspect(BeanUtil.isAspect(clazz))
                .description(BeanUtil.getDescription(clazz))
                .singleton(true)
                .prototype(false)
                .type(clazz)
                .internalBean(true)
                .literalType(clazz)
                .instance(instance)
                .injected(true)
                .build();
        registerBeanDefinition(definition.getName(), definition);
    }

    @Override
    public Set<PluginBeanDefinition> getBeanDefinitionsByType(Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .collect(Collectors.toSet());
    }

    @Override
    public void registerSingletonBeanDefinition(Object instance) {
        registerSingletonBeanDefinition(instance.getClass(), instance);
    }

    @Override
    public void registerSingletonBeanDefinition(Class<?> clazz, Object instance) {
        registerSingletonBeanDefinition(beanNameStrategy.generateBeanName(clazz), clazz, instance);
    }

    @Override
    public void registerBeanDefinition(@NonNull String beanName, @NonNull BeanDefinition beanDefinition) {
        if (!(beanDefinition instanceof PluginBeanDefinition)) {
            throw new BeanCreationException("Unrecognized bean definition type: " + beanDefinition.getClass().getName());
        }
        getBeanDefinitions().add((PluginBeanDefinition) beanDefinition);
    }

    @Override
    public <T> void updateBeanInstance(PluginBeanDefinition definition, T instance) {
        if (definition.isInternalBean()) {
            definition.setLiteralType(instance.getClass());
        } else {
            definition.setType(instance.getClass());
        }
        definition.setInjected(true);
        definition.setInstance(instance);
    }

    @Override
    public Predicate<PluginBeanDefinition> filterBeanDefinition(Class<?> requiredType) {
        return beanDefinition -> {
            val b = getBeanDefinitions().stream().map(PluginBeanDefinition::getName).collect(Collectors.toList());
            if (!beanDefinition.isInternalBean()) {
                return requiredType.isAssignableFrom(beanDefinition.getType());
            }
            return requiredType.isAssignableFrom(beanDefinition.getType()) ||
                    requiredType.isAssignableFrom(beanDefinition.getLiteralType());
        };
    }
}