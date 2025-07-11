package xyz.quartzframework.core.bean.definition;

import lombok.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import xyz.quartzframework.core.bean.BeanInjector;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.bean.definition.metadata.MethodMetadata;
import xyz.quartzframework.core.bean.definition.metadata.TypeMetadata;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.condition.Evaluate;
import xyz.quartzframework.core.condition.Evaluators;
import xyz.quartzframework.core.condition.metadata.*;
import xyz.quartzframework.core.context.annotation.ContextLoads;
import xyz.quartzframework.core.event.Listen;
import xyz.quartzframework.core.task.RepeatedTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

@Getter
@Builder(builderClassName = "PluginBeanDefinitionCreator")
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PluginBeanDefinition extends GenericBeanDefinition implements BeanDefinition, Evaluate {

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
    private Map<Class<? extends Annotation>, List<MethodMetadata>> methods = new HashMap<>();

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

    public void preDestroy(PluginBeanFactory pluginBeanFactory) {
        getPreDestroyMethods().stream().map(MethodMetadata::getMethod).forEach(method -> BeanInjector.newInstance(pluginBeanFactory, method));
        destroy();
    }

    public void destroy() {
        getRepeatedTasksMethods().clear();
        getPreDestroyMethods().clear();
        getPostConstructMethods().clear();
        setInstance(null);
    }

    public void triggerStartMethods(PluginBeanFactory pluginBeanFactory) {
        getContextLoadsMethods().stream().map(MethodMetadata::getMethod).forEach(method -> BeanInjector.newInstance(pluginBeanFactory, method));
    }

    public void construct(PluginBeanFactory pluginBeanFactory) {
        if (!isClassLoaded()) {
            val metadata = getTypeMetadata();
            metadata.getType();
            setClassLoaded(true);
        }
        if (isInitialized() && isSingleton()) {
            return;
        }
        if (instance == null) {
            instance = pluginBeanFactory.getBean(name, typeMetadata.getType());
        }
        if (!isInjected()) {
            BeanInjector.recursiveInjection(pluginBeanFactory, getInstance());
            setInjected(true);
        }
        getPostConstructMethods()
                .stream()
                .map(MethodMetadata::getMethod)
                .forEach(method -> BeanInjector.newInstance(pluginBeanFactory, method));
        constructProvidedBeans(pluginBeanFactory);
        setInitialized(true);
    }

    @Override
    public String[] getDestroyMethodNames() {
        return getPreDestroyMethods().stream().map(MethodMetadata::getName).toArray(String[]::new);
    }

    @Override
    public String[] getInitMethodNames() {
        return getPostConstructMethods().stream().map(MethodMetadata::getName).toArray(String[]::new);
    }

    @Override
    public String getInitMethodName() {
        return getPostConstructMethods().stream().map(MethodMetadata::getName).findAny().orElse("");
    }

    @Override
    public String getDestroyMethodName() {
        return getPostConstructMethods().stream().map(MethodMetadata::getName).findAny().orElse("");
    }

    @Override
    public String getBeanClassName() {
        return typeMetadata.getFullName();
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
    public void setPrimary(boolean primary) {
        setPreferred(primary);
    }

    @Override
    public void setFallback(boolean fallback) {
        setSecondary(fallback);
    }

    @Override
    public boolean isFallback() {
        return isSecondary();
    }

    public boolean isValid(PluginBeanFactory factory) {
        for (val entry : Evaluate.getEvaluators().entrySet()) {
            val type = entry.getKey();
            val evaluator = entry.getValue();
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
            if (shouldEvaluate && !evaluator.evaluate(this, factory)) {
                return false;
            }
        }
        return true;
    }

    public List<MethodMetadata> getProvideMethods() {
        return getMethods().getOrDefault(Provide.class, Collections.emptyList());
    }

    public List<MethodMetadata> getListenMethods() {
        return getMethods().getOrDefault(Listen.class, Collections.emptyList());
    }

    public List<MethodMetadata> getPostConstructMethods() {
        return getMethods().getOrDefault(PostConstruct.class, Collections.emptyList());
    }

    public List<MethodMetadata> getPreDestroyMethods() {
        return getMethods().getOrDefault(PreDestroy.class, Collections.emptyList());
    }

    public List<MethodMetadata> getContextLoadsMethods() {
        return getMethods().getOrDefault(ContextLoads.class, Collections.emptyList());
    }

    public List<MethodMetadata> getRepeatedTasksMethods() {
        return getMethods().getOrDefault(RepeatedTask.class, Collections.emptyList());
    }

    public List<MethodMetadata> getLifecycleMethods() {
        val methods = new ArrayList<MethodMetadata>();
        methods.addAll(getPostConstructMethods());
        methods.addAll(getContextLoadsMethods());
        methods.addAll(getPreDestroyMethods());
        return methods;
    }

    public void provideMethods(PluginBeanDefinitionRegistry registry,
                               PluginBeanDefinitionBuilder builder,
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
            val beanMethods = new HashMap<Class<? extends Annotation>, List<MethodMetadata>>();
            Function<String, Boolean> isValidLifecycleMethodName = (candidate) ->
                    candidate != null &&
                    !candidate.isBlank() &&
                    !candidate.equalsIgnoreCase("<none>");
            if (isValidLifecycleMethodName.apply(initMethodName)) {
                val methodMetadata = metadata.getMethodMap().get(initMethodName);
                beanMethods.put(PostConstruct.class, List.of(methodMetadata));
            }
            if (isValidLifecycleMethodName.apply(destroyMethodName)) {
                val methodMetadata = metadata.getMethodMap().get(destroyMethodName);
                 beanMethods.put(PreDestroy.class, List.of(methodMetadata));
            }
            beanMethods.put(Provide.class, List.of(method));
            val definition = PluginBeanDefinition
                    .builder()
                    .name(name)
                    .internalBean(false)
                    .typeMetadata(metadata)
                    .methods(beanMethods)
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

    private void constructProvidedBeans(PluginBeanFactory pluginBeanFactory) {
        val registry = pluginBeanFactory.getRegistry();
        getProvideMethods()
                .stream()
                .map(method -> {
                    val candidate = TypeMetadata.of(method);
                    return registry.getBeanDefinition(candidate);
                })
                .filter(d -> !d.isInternalBean())
                .filter(d -> !d.isInjected())
                .sorted(Comparator.comparingInt(PluginBeanDefinition::getOrder).reversed())
                .forEach(definition -> definition.construct(pluginBeanFactory));
    }
}