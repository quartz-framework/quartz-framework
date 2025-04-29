package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.condition.metadata.*;
import dev.pk7r.spigot.starter.core.property.PropertyPostProcessor;
import dev.pk7r.spigot.starter.core.util.ClassUtil;
import lombok.val;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public interface Evaluate {

    GenericConditionMetadata getGenericConditionMetadata();

    PropertyConditionMetadata getPropertyConditionMetadata();

    BeanConditionMetadata getBeanConditionMetadata();

    BeanConditionMetadata getMissingBeanConditionMetadata();

    ClassConditionMetadata getClassConditionMetadata();

    ClassConditionMetadata getMissingClassConditionMetadata();

    AnnotationConditionMetadata getAnnotationConditionMetadata();

    default Map<ConditionType, ConditionEvaluator> getEvaluators() {
        Map<ConditionType, ConditionEvaluator> evaluators = new HashMap<>();
        evaluators.put(ConditionType.CONDITIONAL, (def, factory) -> {
            val cond = def.getGenericConditionMetadata();
            return cond == null || factory.getBean(cond.getValue()).test();
        });
        evaluators.put(ConditionType.ON_CLASS, (def, factory) -> {
            val metadata = def.getClassConditionMetadata();
            return metadata == null || metadata.getClassNames().stream().allMatch(n -> ClassUtil.isClassLoaded(n, factory.getClassLoader()));
        });
        evaluators.put(ConditionType.ON_MISSING_CLASS, (def, factory) -> {
            val metadata = def.getMissingClassConditionMetadata();
            return metadata == null || metadata.getClassNames().stream().noneMatch(n -> ClassUtil.isClassLoaded(n, factory.getClassLoader()));
        });
        evaluators.put(ConditionType.ON_BEAN, (def, factory) -> {
            val metadata = def.getBeanConditionMetadata();
            return metadata == null || Arrays
                    .stream(metadata.getClasses())
                    .noneMatch(req -> factory.getRegistry().getBeanDefinitionsByType(req).isEmpty());
        });
        evaluators.put(ConditionType.ON_MISSING_BEAN, (def, factory) -> {
            val metadata = def.getMissingBeanConditionMetadata();
            if (metadata == null) {
                return true;
            }
            val requiredTypes = Arrays.asList(metadata.getClasses());
            return requiredTypes.stream()
                    .noneMatch(requiredType -> factory
                            .getRegistry()
                            .getBeanDefinitions()
                            .stream()
                            .filter(b -> !b.getId().equals(def.getId()))
                            .anyMatch(factory.getRegistry().filterBeanDefinition(requiredType)));
        });
        evaluators.put(ConditionType.ON_PROPERTY, (def, factory) -> {
            val metadata = def.getPropertyConditionMetadata();
            if (metadata == null) return true;
            val env = factory.getBean(PropertyPostProcessor.class);
            val value = env.process(metadata.getProperty().value(), metadata.getProperty().source(), String.class);
            return Objects.equals(value, metadata.getExpected());
        });
        evaluators.put(ConditionType.ON_ANNOTATION, (def, factory) -> {
            val metadata = def.getAnnotationConditionMetadata();
            if (metadata == null) return true;
            return Arrays.stream(metadata.getClasses()).noneMatch(a -> factory.getBeansWithAnnotation(a).isEmpty());
        });
        return evaluators;
    }
}