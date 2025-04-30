package xyz.quartzframework.core.bean;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.condition.Evaluate;
import xyz.quartzframework.core.condition.Evaluators;
import xyz.quartzframework.core.condition.metadata.*;
import xyz.quartzframework.core.util.InjectionUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PluginBeanDefinition extends GenericBeanDefinition implements BeanDefinition, Evaluate {

    @NonNull
    @Builder.Default
    @EqualsAndHashCode.Include
    private final UUID id = UUID.randomUUID();

    @Builder.Default
    private int order = Integer.MAX_VALUE;

    @Setter
    @Builder.Default
    private boolean injected = false;

    @NonNull
    private String name;

    private String description;

    private boolean aspect;

    @Setter
    private Object instance;

    @NonNull
    @Setter
    private Class<?> type;

    @Setter
    private boolean preferred;

    @Setter
    private boolean deferred;

    private boolean internalBean;

    private boolean namedInstance;

    private boolean proxied;

    @Builder.Default
    private List<String> environments = Evaluators.DEFAULT_PROFILES;

    private boolean contextBootstrapper;

    private boolean bootstrapper;

    private boolean singleton;

    private boolean prototype;

    private boolean configurer;

    @Setter
    private boolean initialized;

    @Setter
    private Class<?> literalType;

    @Builder.Default
    private List<Method> postConstructMethods = new ArrayList<>();

    @Builder.Default
    private List<Method> preDestroyMethods = new ArrayList<>();

    @Builder.Default
    private List<Method> listenMethods = new ArrayList<>();

    @Builder.Default
    private List<Method> repeatedTasksMethods = new ArrayList<>();

    private GenericConditionMetadata genericConditionMetadata;

    private AnnotationConditionMetadata annotationConditionMetadata;

    private PropertyConditionMetadata propertyConditionMetadata;

    private BeanConditionMetadata beanConditionMetadata;

    private BeanConditionMetadata missingBeanConditionMetadata;

    private ClassConditionMetadata classConditionMetadata;

    private ClassConditionMetadata missingClassConditionMetadata;

    public void preDestroy(PluginBeanFactory pluginBeanFactory) {
        getPreDestroyMethods().forEach(method -> InjectionUtil.newInstance(pluginBeanFactory, method));
        destroy();
    }

    public void destroy() {
        getRepeatedTasksMethods().clear();
        getPreDestroyMethods().clear();
        getPostConstructMethods().clear();
        setInstance(null);
    }

    public void construct(PluginBeanFactory pluginBeanFactory) {
        if (isInitialized() && isSingleton()) {
            return;
        }
        if (instance == null) {
            instance = pluginBeanFactory.getBean(type);
        }
        if (!isInjected()) {
            InjectionUtil.recursiveInjection(pluginBeanFactory, getInstance());
            setInjected(true);
        }
        val registry = pluginBeanFactory.getRegistry();
        getPostConstructMethods().forEach(method -> InjectionUtil.newInstance(pluginBeanFactory, method));
        List<PluginBeanDefinition> methodBeans = registry.getBeanDefinitions()
                .stream()
                .filter(d -> !d.isInternalBean())
                .filter(d -> d.getLiteralType().equals(this.literalType))
                .filter(d -> !d.isInjected())
                .sorted(Comparator.comparingInt(PluginBeanDefinition::getOrder))
                .collect(Collectors.toList());
        for (PluginBeanDefinition methodBean : methodBeans) {
            methodBean.construct(pluginBeanFactory);
        }
        setInitialized(true);
    }

    @Override
    public String[] getDestroyMethodNames() {
        return getPreDestroyMethods().stream().map(Method::getName).toArray(String[]::new);
    }

    @Override
    public String[] getInitMethodNames() {
        return getPostConstructMethods().stream().map(Method::getName).toArray(String[]::new);
    }

    @Override
    public String getInitMethodName() {
        return getPostConstructMethods().stream().map(Method::getName).findAny().orElse("");
    }

    @Override
    public String getDestroyMethodName() {
        return getPostConstructMethods().stream().map(Method::getName).findAny().orElse("");
    }

    @Override
    public String getBeanClassName() {
        return getLiteralType().getName();
    }

    @Override
    public void setLazyInit(boolean lazyInit) {
        setDeferred(lazyInit);
    }

    @Override
    public boolean isLazyInit() {
        return this.deferred;
    }

    @Override
    public void setPrimary(boolean primary) {
        setPreferred(primary);
    }

    public boolean isValid(PluginBeanFactory factory) {
        return Evaluate.getEvaluators().values().stream().allMatch(eval -> eval.evaluate(this, factory));
    }
}