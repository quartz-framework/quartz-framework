package xyz.quartzframework.beans.definition;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import xyz.quartzframework.beans.condition.BeanEvaluationMomentType;
import xyz.quartzframework.beans.condition.Evaluate;
import xyz.quartzframework.beans.condition.Evaluators;
import xyz.quartzframework.beans.condition.metadata.*;
import xyz.quartzframework.beans.definition.metadata.MethodMetadata;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.beans.strategy.BeanNameStrategy;
import xyz.quartzframework.beans.support.BeanInjector;
import xyz.quartzframework.beans.support.annotation.Provide;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Builder(builderClassName = "PluginBeanDefinitionCreator")
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class QuartzBeanDefinition implements BeanDefinition, Evaluate {

    private final Map<String, Object> attributes = new HashMap<>();

    @Nullable
    private QuartzBeanDefinition delegate;

    @NonNull
    @Builder.Default
    @EqualsAndHashCode.Include
    private final UUID id = UUID.randomUUID();

    @Builder.Default
    private int order = Ordered.HIGHEST_PRECEDENCE;

    @Setter
    @Builder.Default
    private boolean injected = false;

    @Setter
    @Builder.Default
    private boolean initialized = false;

    @Setter
    @Builder.Default
    private boolean classLoaded = false;

    @Setter
    @NonNull
    private String name;

    private String description;

    @Builder.Default
    private boolean aspect = false;

    @Builder.Default
    private boolean contextBootstrapper = false;

    @Builder.Default
    private boolean bootstrapper = false;

    @Builder.Default
    private boolean configurer = false;

    @Setter
    @Nullable
    private Object instance;

    @NonNull
    @Setter
    private TypeMetadata typeMetadata;

    @Setter
    private boolean preferred;

    @Setter
    private boolean secondary;

    @Setter
    private boolean deferred;

    private boolean internalBean;

    private boolean namedInstance;

    private boolean proxied;

    @Builder.Default
    private List<String> environments = Evaluators.DEFAULT_PROFILES;

    @Setter
    private boolean singleton;

    @Setter
    private boolean prototype;

    @Builder.Default
    private List<MethodMetadata> methods = new ArrayList<>();

    @Nullable
    private GenericConditionMetadata genericConditionMetadata;

    @Nullable
    private AnnotationConditionMetadata annotationConditionMetadata;

    @Nullable
    private PropertyConditionMetadata propertyConditionMetadata;

    @Nullable
    private BeanConditionMetadata beanConditionMetadata;

    @Nullable
    private BeanConditionMetadata missingBeanConditionMetadata;

    @Nullable
    private ClassConditionMetadata classConditionMetadata;

    @Nullable
    private ClassConditionMetadata missingClassConditionMetadata;

    public void preDestroy(QuartzBeanFactory quartzBeanFactory) {
        getPreDestroyMethods().stream().map(MethodMetadata::getMethod).forEach(method -> BeanInjector.newInstance(quartzBeanFactory, method));
        destroy();
    }

    public void destroy() {
        getMethods().clear();
        setInstance(null);
    }

    public void triggerMethods(QuartzBeanFactory quartzBeanFactory, Predicate<MethodMetadata> predicate) {
        getMethods().stream().filter(predicate).map(MethodMetadata::getMethod).forEach(method -> BeanInjector.newInstance(quartzBeanFactory, method));
    }

    public void construct(QuartzBeanFactory quartzBeanFactory) {
        if (!isClassLoaded()) {
            val metadata = getTypeMetadata();
            metadata.getType();
            setClassLoaded(true);
        }
        if (isInitialized() && isSingleton()) {
            return;
        }
        if (instance == null && quartzBeanFactory.getRegistry().containsBeanDefinition(typeMetadata)) {
            instance = quartzBeanFactory.getBean(name, typeMetadata.getType());
        }
        if (!isInjected()) {
            BeanInjector.recursiveInjection(quartzBeanFactory, getInstance());
            setInjected(true);
        }
        getPostConstructMethods()
                .stream()
                .map(MethodMetadata::getMethod)
                .forEach(method -> BeanInjector.newInstance(quartzBeanFactory, method));
        constructProvidedBeans(quartzBeanFactory);
        setInitialized(true);
    }

    public String[] getDestroyMethodNames() {
        return getPreDestroyMethods().stream().map(MethodMetadata::getName).toArray(String[]::new);
    }

    public String[] getInitMethodNames() {
        return getPostConstructMethods().stream().map(MethodMetadata::getName).toArray(String[]::new);
    }

    @Override
    public String getInitMethodName() {
        return getPostConstructMethods().stream().map(MethodMetadata::getName).findAny().orElse("");
    }

    @Override
    public void setDestroyMethodName(@Nullable String destroyMethodName) {

    }

    @Override
    public String getDestroyMethodName() {
        return getPostConstructMethods().stream().map(MethodMetadata::getName).findAny().orElse("");
    }

    @Override
    public void setRole(int role) {

    }

    @Override
    public int getRole() {
        return BeanDefinition.ROLE_APPLICATION;
    }

    @Override
    public void setDescription(@Nullable String description) {
        if (description != null) {
            this.description = description;
        }
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forInstance(getInstance());
    }

    @Override
    public boolean isAbstract() {
        return getTypeMetadata().isClassBean();
    }

    @Override
    public String getResourceDescription() {
        return getDescription();
    }

    @Nullable
    @Override
    public BeanDefinition getOriginatingBeanDefinition() {
        return getDelegate();
    }

    @Override
    public void setParentName(@Nullable String parentName) {

    }

    @Override
    public String getParentName() {
        return getName();
    }

    @Override
    public void setBeanClassName(@Nullable String beanClassName) {

    }

    @Override
    public String getBeanClassName() {
        return typeMetadata.getFullName();
    }

    @Override
    public void setScope(@Nullable String scope) {
        if (scope == null) {
            setSingleton(true);
            return;
        }
        if (Objects.equals(scope, SCOPE_SINGLETON)) {
            setSingleton(true);
        }
        if (Objects.equals(scope, SCOPE_PROTOTYPE)) {
            setPrototype(true);
            setSingleton(false);
        }
    }

    @Override
    public String getScope() {
        return isSingleton() ? SCOPE_SINGLETON : isPrototype() ? SCOPE_PROTOTYPE : SCOPE_SINGLETON;
    }

    @Override
    public void setLazyInit(boolean lazyInit) {
        setDeferred(lazyInit);
    }

    @Override
    public boolean isLazyInit() {
        return isDeferred();
    }

    @Override
    public void setDependsOn(@Nullable String... dependsOn) {

    }

    @Override
    public String[] getDependsOn() {
        return new String[0];
    }

    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {
        setInitialized(autowireCandidate);
    }

    @Override
    public boolean isAutowireCandidate() {
        return isInitialized();
    }

    @Override
    public void setPrimary(boolean primary) {
        setPreferred(primary);
    }

    @Override
    public boolean isPrimary() {
        return isPreferred();
    }

    @Override
    public void setFallback(boolean fallback) {
        setSecondary(fallback);
    }

    @Override
    public boolean isFallback() {
        return isSecondary();
    }

    @Override
    public void setFactoryBeanName(@Nullable String factoryBeanName) {

    }

    @Override
    public String getFactoryBeanName() {
        return getTypeMetadata().getDeclaredByClass();
    }

    @Override
    public void setFactoryMethodName(@Nullable String factoryMethodName) {

    }

    @Override
    public String getFactoryMethodName() {
        return getTypeMetadata().getDeclaredByMethod();
    }

    @Override
    public ConstructorArgumentValues getConstructorArgumentValues() {
        return new ConstructorArgumentValues();
    }

    @Override
    public MutablePropertyValues getPropertyValues() {
        return new MutablePropertyValues();
    }

    @NonNull
    @Override
    public void setInitMethodName(@Nullable String initMethodName) {

    }

    public boolean isInvalid(QuartzBeanFactory factory, BeanEvaluationMomentType evaluationMomentType) {
        for (val entry : Evaluate.getEvaluators().entrySet()) {
            val type = entry.getKey();
            val evaluator = entry.getValue();
            val canEvaluate = type.getEvaluationMomentType().equals(evaluationMomentType);
            boolean shouldEvaluate = switch (type) {
                case CONDITIONAL -> genericConditionMetadata != null;
                case ON_CLASS -> classConditionMetadata != null;
                case ON_MISSING_CLASS -> missingClassConditionMetadata != null;
                case ON_BEAN -> beanConditionMetadata != null;
                case ON_MISSING_BEAN -> missingBeanConditionMetadata != null;
                case ON_PROPERTY -> propertyConditionMetadata != null;
                case ON_ENVIRONMENT -> environments != null && !environments.isEmpty();
                case ON_ANNOTATION -> annotationConditionMetadata != null;
            };
            if (shouldEvaluate && canEvaluate && !evaluator.evaluate(this, factory)) {
                return true;
            }
        }
        return false;
    }

    public List<MethodMetadata> getProvideMethods() {
        return getMethods(method -> method.hasAnnotation(Provide.class));
    }

    public List<MethodMetadata> getPostConstructMethods() {
        return getMethods(method -> method.hasAnnotation(PostConstruct.class));
    }

    public List<MethodMetadata> getPreDestroyMethods() {
        return getMethods(method -> method.hasAnnotation(PreDestroy.class));
    }

    public List<MethodMetadata> getMethods(Predicate<MethodMetadata> predicate) {
        return getMethods().stream().filter(predicate).collect(Collectors.toList());
    }

    public List<MethodMetadata> getLifecycleMethods() {
        val methods = new ArrayList<MethodMetadata>();
        methods.addAll(getPostConstructMethods());
        methods.addAll(getPreDestroyMethods());
        return methods;
    }

    public void provideMethods(QuartzBeanDefinitionRegistry registry,
                               QuartzBeanDefinitionBuilder builder,
                               BeanNameStrategy beanNameStrategy) {
        val methods = getProvideMethods();
        for (val method : methods) {
            val metadata = TypeMetadata.of(method);
            val annotation = metadata.getAnnotation(Provide.class);
            if (annotation.isEmpty()) continue;
            val initMethodName = annotation.get().getAttribute("initMethodName", String.class);
            val destroyMethodName = annotation.get().getAttribute("destroyMethodName", String.class);
            val returnMetadata = method.getReturnType();
            val name = beanNameStrategy.generateBeanName(metadata);
            val beanMethods = new ArrayList<MethodMetadata>();
            Function<String, Boolean> isValidLifecycleMethodName = (candidate) ->
                    candidate != null &&
                    !candidate.isBlank() &&
                    !candidate.equalsIgnoreCase("<none>");
            if (isValidLifecycleMethodName.apply(initMethodName)) {
                val methodMetadata = metadata.getMethodMap().get(initMethodName);
                beanMethods.add(methodMetadata);
            }
            if (isValidLifecycleMethodName.apply(destroyMethodName)) {
                val methodMetadata = metadata.getMethodMap().get(destroyMethodName);
                beanMethods.add(methodMetadata);
            }
            beanMethods.add(method);
            val definition = QuartzBeanDefinition
                    .builder()
                    .name(name)
                    .internalBean(false)
                    .typeMetadata(metadata)
                    .methods(beanMethods)
                    .delegate(this)
                    .order(builder.getOrder(metadata))
                    .preferred(builder.isPreferred(metadata))
                    .secondary(builder.isSecondary(metadata))
                    .deferred(builder.isDeferred(metadata))
                    .singleton(builder.isSingleton(metadata))
                    .prototype(builder.isPrototype(metadata))
                    .description(builder.getDescription(metadata))
                    .environments(builder.getEnvironments(metadata))
                    .proxied(isProxied() && builder.isProxied(metadata))
                    .namedInstance(builder.isNamedInstance(metadata))
                    .aspect(builder.isAspect(returnMetadata))
                    .beanConditionMetadata(BeanConditionMetadata.ofPresent(metadata))
                    .missingBeanConditionMetadata(BeanConditionMetadata.ofMissing(metadata))
                    .classConditionMetadata(ClassConditionMetadata.ofPresent(metadata))
                    .missingClassConditionMetadata(ClassConditionMetadata.ofMissing(metadata))
                    .propertyConditionMetadata(PropertyConditionMetadata.of(metadata))
                    .annotationConditionMetadata(AnnotationConditionMetadata.of(metadata))
                    .genericConditionMetadata(GenericConditionMetadata.of(metadata))
                    .build();
            registry.registerBeanDefinition(definition.getName(), definition);
        }
    }

    private void constructProvidedBeans(QuartzBeanFactory quartzBeanFactory) {
        val registry = quartzBeanFactory.getRegistry();
        getProvideMethods()
                .stream()
                .map(method -> {
                    val candidate = TypeMetadata.of(method);
                    if (!registry.containsBeanDefinition(candidate)) {
                        return null;
                    }
                    return registry.getBeanDefinition(candidate);
                })
                .filter(Objects::nonNull)
                .filter(d -> !d.isInternalBean())
                .filter(d -> !d.isInjected())
                .sorted(Comparator.comparingInt(QuartzBeanDefinition::getOrder).reversed())
                .forEach(definition -> definition.construct(quartzBeanFactory));
    }

    @Override
    public void setAttribute(String name, @Nullable Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    @Override
    public String[] attributeNames() {
        return attributes.keySet().toArray(new String[0]);
    }
}