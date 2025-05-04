package xyz.quartzframework.core.bean.registry;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.pacesys.reflect.Reflect;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.ResolvableType;
import xyz.quartzframework.core.bean.PluginBeanDefinition;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.condition.annotation.*;
import xyz.quartzframework.core.condition.metadata.*;
import xyz.quartzframework.core.context.annotation.ContextLoads;
import xyz.quartzframework.core.event.Listen;
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

@Slf4j
@NoProxy
@Getter
@AllArgsConstructor
public class DefaultPluginBeanDefinitionRegistry implements PluginBeanDefinitionRegistry {

    private final BeanNameStrategy beanNameStrategy;

    private final Set<PluginBeanDefinition> beanDefinitions = new HashSet<>();

    private static final Comparator<PluginBeanDefinition> PREFERRED_COMPARATOR = Comparator
            .comparingInt((PluginBeanDefinition def) -> {
                if (def.isPreferred()) return 0;
                if (!def.isSecondary()) return 1;
                return 2;
            });

    @NonNull
    @Override
    public PluginBeanDefinition getBeanDefinition(@NonNull String beanName) {
        val matches = getBeanDefinitions()
                .stream()
                .filter(b -> b.getName().equals(beanName))
                .toList();
        if (matches.isEmpty()) {
            throw new BeanNotFoundException("No beans found for " + beanName);
        }
        return resolveUniqueDefinition(matches);
    }

    @Override
    public boolean containsBeanDefinition(@NonNull String beanName) {
        return getBeanDefinitions()
                .stream()
                .anyMatch(b -> b.getName().equals(beanName));
    }

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
        val candidates = getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(requiredType))
                .toList();
        if (candidates.isEmpty()) {
            throw new BeanNotFoundException("No beans found for " + requiredType.getSimpleName());
        }
        return resolveUniqueDefinition(candidates);
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
                .secondary(BeanUtil.isSecondary(clazz))
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
                .resolvableType(ResolvableType.forClass(clazz))
                .beanConditionMetadata(BeanConditionMetadata.of(clazz.getAnnotation(ActivateWhenBeanPresent.class)))
                .missingBeanConditionMetadata(BeanConditionMetadata.of(clazz.getAnnotation(ActivateWhenBeanMissing.class)))
                .classConditionMetadata(ClassConditionMetadata.of(clazz.getAnnotation(ActivateWhenClassPresent.class)))
                .missingClassConditionMetadata(ClassConditionMetadata.of(clazz.getAnnotation(ActivateWhenClassMissing.class)))
                .propertyConditionMetadata(PropertyConditionMetadata.of(clazz.getAnnotation(ActivateWhenPropertyEquals.class)))
                .annotationConditionMetadata(AnnotationConditionMetadata.of(clazz.getAnnotation(ActivateWhenAnnotationPresent.class)))
                .genericConditionMetadata(GenericConditionMetadata.of(clazz.getAnnotation(ActivateWhen.class)))
                .lifecycleMethods(lifecycleMethods)
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
                    .secondary(BeanUtil.isSecondary(method))
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
                    .resolvableType(ResolvableType.forMethodReturnType(method))
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

    @SuppressWarnings("unchecked")
    private Map<Class<? extends Annotation>, List<Method>> mapLifecycleMethods(Class<?> clazz) {
        Class<? extends Annotation>[] annotations = new Class[]{
                PostConstruct.class,
                ContextLoads.class,
                PreDestroy.class,
                RepeatedTask.class,
        };
        val methods = ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, clazz, annotations);
        val map = new HashMap<Class<? extends Annotation>, List<Method>>();
        for (val method : methods) {
            if (method.getParameterCount() > 0 || !method.getReturnType().equals(Void.TYPE)) {
                throw new LifecycleException("Lifecycle methods must have no parameters and return void");
            }
            for (val annotation : annotations) {
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
                .secondary(BeanUtil.isSecondary(clazz))
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
                .resolvableType(ResolvableType.forClass(clazz))
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
                .sorted(PREFERRED_COMPARATOR)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
        if (!(beanDefinition instanceof PluginBeanDefinition pluginBeanDefinition)) {
            throw new BeanCreationException("Unrecognized bean definition type: " + beanDefinition.getClass().getName());
        }
        if (pluginBeanDefinition.isPreferred() && pluginBeanDefinition.isSecondary()) {
            log.warn("Bean '{}' is annotated as both @Preferred and @Secondary — ignoring @Secondary.", pluginBeanDefinition.getName());
            pluginBeanDefinition.setSecondary(false);
        }
        getBeanDefinitions().add(pluginBeanDefinition);
    }

    @Override
    public <T> void updateBeanInstance(PluginBeanDefinition definition, T instance) {
        if (definition.isInternalBean()) {
            definition.setLiteralType(instance.getClass());
        } else {
            definition.setType(instance.getClass());
        }
        definition.setResolvableType(ResolvableType.forInstance(instance));
        definition.setInjected(true);
        definition.setInstance(instance);
    }

    @Override
    public Predicate<PluginBeanDefinition> filterBeanDefinition(Class<?> requiredType) {
        return beanDefinition -> {
            val b = getBeanDefinitions().stream().map(PluginBeanDefinition::getName).toList();
            if (!beanDefinition.isInternalBean()) {
                return requiredType.isAssignableFrom(beanDefinition.getType()) || beanDefinition.getResolvableType().isAssignableFrom(requiredType);
            }
            return requiredType.isAssignableFrom(beanDefinition.getType()) ||
                    requiredType.isAssignableFrom(beanDefinition.getLiteralType())
                    || beanDefinition.getResolvableType().isAssignableFrom(requiredType);
        };
    }

    private PluginBeanDefinition resolveUniqueDefinition(List<PluginBeanDefinition> candidates) {
        if (candidates.size() <= 1) return candidates.get(0);
        val preferred = candidates.stream().filter(PluginBeanDefinition::isPreferred).toList();
        if (preferred.size() > 1) {
            log.warn("Multiple @Preferred beans found: {}", preferred.stream().map(PluginBeanDefinition::getName).toList());
        }
        if (!preferred.isEmpty()) return preferred.get(0);
        val normal = candidates.stream()
                .filter(d -> !d.isPreferred() && !d.isSecondary())
                .toList();
        val ambiguousNormals = normal.stream()
                .filter(d -> !d.isNamedInstance())
                .toList();
        if (ambiguousNormals.size() > 1) {
            log.warn("Multiple unnamed normal beans found (no @Preferred/@Secondary/@NamedInstance): '{}'", ambiguousNormals.stream().map(PluginBeanDefinition::getName).collect(Collectors.joining(", ")));
        }
        if (!normal.isEmpty()) return normal.get(0);
        val secondary = candidates.stream().filter(PluginBeanDefinition::isSecondary).toList();
        if (secondary.size() > 1) {
            log.warn("Multiple @Secondary beans found: {}", secondary.stream().map(PluginBeanDefinition::getName).toList());
        }
        return secondary.get(0);
    }
}