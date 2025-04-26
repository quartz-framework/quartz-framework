package dev.pk7r.spigot.starter.core.bean;

import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.condition.*;
import dev.pk7r.spigot.starter.core.util.InjectionUtil;
import lombok.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class BeanDefinition {

    @NonNull
    @Builder.Default
    private final UUID id = UUID.randomUUID();

    @NonNull
    private String name;

    @NonNull
    private BeanScope scope;

    @Setter
    private Object instance;

    @NonNull
    private Class<?> type;

    private boolean primary;

    private boolean internalBean;

    private boolean namedInstance;

    private boolean lazy;

    private boolean autoConfiguration;

    private Class<?> literalType;

    private List<Method> postConstructMethods;

    private List<Method> preDestroyMethods;

    private List<Method> repeatedTasksMethods;

    private ConditionalOnClassMetadata conditionalOnClass;

    private ConditionalOnMissingClassMetadata conditionalOnMissingClass;

    private ConditionalOnBeanMetadata conditionalOnBean;

    private ConditionalOnMissingBeanMetadata conditionalOnMissingBean;

    private ConditionalOnValueMetadata conditionalOnValue;

    private ConditionalOnAnnotationMetadata conditionalOnAnnotation;

    public void preDestroy(BeanFactory beanFactory) {
        getPreDestroyMethods().forEach(method -> InjectionUtil.newInstance(beanFactory, method));
        destroy();
    }

    public void destroy() {
        getRepeatedTasksMethods().clear();
        getPreDestroyMethods().clear();
        getPostConstructMethods().clear();
    }

    public void construct(BeanFactory beanFactory) {
        InjectionUtil.recursiveInjection(beanFactory, literalType);
        getPostConstructMethods().forEach(method -> InjectionUtil.newInstance(beanFactory, method));
    }
}