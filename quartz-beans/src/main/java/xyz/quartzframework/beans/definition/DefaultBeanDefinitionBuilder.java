package xyz.quartzframework.beans.definition;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.Nullable;
import xyz.quartzframework.beans.condition.metadata.*;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.beans.strategy.BeanNameStrategy;
import xyz.quartzframework.beans.support.exception.LifecycleException;

import java.util.UUID;

@RequiredArgsConstructor
public class DefaultBeanDefinitionBuilder implements QuartzBeanDefinitionBuilder {

    private final QuartzBeanFactory quartzBeanFactory;

    private final BeanNameStrategy beanNameStrategy;

    @Override
    @Nullable
    public QuartzBeanDefinition create(TypeMetadata metadata, @Nullable QuartzBeanDefinition delegate, boolean external) {
        val generatedName = beanNameStrategy.generateBeanName(metadata);
        val isPreferred = isPreferred(metadata);
        val isSingleton = isSingleton(metadata);
        val builder = QuartzBeanDefinition
                .builder()
                .injected(false)
                .initialized(false)
                .id(UUID.randomUUID())
                .typeMetadata(metadata);
        val methods = findMethods(metadata);
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
        builder.delegate(delegate);
        builder.annotationConditionMetadata(AnnotationConditionMetadata.of(metadata));
        builder.classConditionMetadata(ClassConditionMetadata.ofPresent(metadata));
        builder.missingClassConditionMetadata(ClassConditionMetadata.ofMissing(metadata));
        builder.beanConditionMetadata(BeanConditionMetadata.ofPresent(metadata));
        builder.missingBeanConditionMetadata(BeanConditionMetadata.ofMissing(metadata));
        builder.propertyConditionMetadata(PropertyConditionMetadata.of(metadata));
        builder.genericConditionMetadata(GenericConditionMetadata.of(metadata));
        builder.internalBean(!external);
        val build = builder.build();
        if (!build.isValid(quartzBeanFactory)) {
            return null;
        }
        validate(build);
        return build;
    }

    private void validate(QuartzBeanDefinition beanDefinition) {
//        val listenMethods = beanDefinition.getListenMethods();
//        for (MethodMetadata listenMethod : listenMethods) {
//            if (listenMethod.getParameterCount() != 1) {
//                throw new LifecycleException("@Listen methods must have exactly one parameter");
//            }
//            if (!listenMethod.isVoid()) {
//                throw new LifecycleException("@Listen methods must return void");
//            }
//        }
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