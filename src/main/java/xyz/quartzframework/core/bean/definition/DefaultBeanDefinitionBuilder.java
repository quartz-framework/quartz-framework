package xyz.quartzframework.core.bean.definition;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.Nullable;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.bean.definition.metadata.MethodMetadata;
import xyz.quartzframework.core.bean.definition.metadata.TypeMetadata;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.condition.metadata.*;
import xyz.quartzframework.core.context.annotation.ContextLoads;
import xyz.quartzframework.core.event.Listen;
import xyz.quartzframework.core.exception.LifecycleException;
import xyz.quartzframework.core.task.RepeatedTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultBeanDefinitionBuilder implements PluginBeanDefinitionBuilder {

    private final PluginBeanFactory pluginBeanFactory;

    private final BeanNameStrategy beanNameStrategy;

    @Override
    @Nullable
    public PluginBeanDefinition create(TypeMetadata metadata, boolean external) {
        val generatedName = beanNameStrategy.generateBeanName(metadata);
        val isPreferred = isPreferred(metadata);
        val isSingleton = isSingleton(metadata);
        val builder = PluginBeanDefinition
                .builder()
                .injected(false)
                .initialized(false)
                .id(UUID.randomUUID())
                .typeMetadata(metadata);
        val desiredMethods = List.of(
                PostConstruct.class,
                PreDestroy.class,
                ContextLoads.class,
                RepeatedTask.class,
                Listen.class,
                Provide.class);
        val methods = findMethods(metadata, desiredMethods);
        builder.name(generatedName);
        builder.aspect(isAspect(metadata));
        builder.configurer(isConfigurer(metadata));
        builder.bootstrapper(isBootstrapper(metadata));
        builder.contextBootstrapper(isContextBootstrapper(metadata));
        builder.description(getDescription(metadata));
        builder.deferred(isDeferred(metadata));
        builder.preferred(isPreferred);
        builder.secondary(!isPreferred && isSecondary(metadata));
        builder.singleton(isSingleton);
        builder.prototype(!isSingleton && isPrototype(metadata));
        builder.namedInstance(isNamedInstance(metadata));
        builder.proxied(isProxied(metadata));
        builder.environments(getEnvironments(metadata));
        builder.order(getOrder(metadata));
        builder.methods(methods);
        builder.annotationConditionMetadata(AnnotationConditionMetadata.of(metadata));
        builder.classConditionMetadata(ClassConditionMetadata.ofPresent(metadata));
        builder.missingClassConditionMetadata(ClassConditionMetadata.ofMissing(metadata));
        builder.beanConditionMetadata(BeanConditionMetadata.ofPresent(metadata));
        builder.missingBeanConditionMetadata(BeanConditionMetadata.ofMissing(metadata));
        builder.propertyConditionMetadata(PropertyConditionMetadata.of(metadata));
        builder.genericConditionMetadata(GenericConditionMetadata.of(metadata));
        builder.internalBean(!external);
        val build = builder.build();
        if (!build.isValid(pluginBeanFactory)) {
            return null;
        }
        validate(build);
        return build;
    }

    private void validate(PluginBeanDefinition beanDefinition) {
        val listenMethods = beanDefinition.getListenMethods();
        for (MethodMetadata listenMethod : listenMethods) {
            if (listenMethod.getParameterCount() != 1) {
                throw new LifecycleException("@Listen methods must have exactly one parameter");
            }
            if (!listenMethod.isVoid()) {
                throw new LifecycleException("@Listen methods must return void");
            }
        }
        val lifecycleMethods = beanDefinition.getLifecycleMethods();
        for (val methodInfo : lifecycleMethods) {
            if (methodInfo.getParameterCount() > 0) {
                throw new LifecycleException("Lifecycle methods must have no parameters");
            }
            if (!methodInfo.isVoid()) {
                throw new LifecycleException("Lifecycle methods must return void");
            }
        }
    }
}