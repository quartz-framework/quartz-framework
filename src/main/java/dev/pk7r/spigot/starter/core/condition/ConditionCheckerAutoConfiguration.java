package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.Primary;
import dev.pk7r.spigot.starter.core.bean.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.property.PropertyPostProcessor;
import dev.pk7r.spigot.starter.core.util.BeanUtil;
import dev.pk7r.spigot.starter.core.util.ClassUtil;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
@AutoConfiguration(force = true)
public class ConditionCheckerAutoConfiguration {

    private final BeanDefinitionRegistry beanDefinitionRegistry;

    @Bean
    @Primary
    ConditionalOnAnnotationChecker conditionalOnAnnotationConditionChecker() {
        return (annotation, beanDefinition) -> {
            if (annotation == null) return true;
            val requiredAnnotations = Arrays.asList(annotation.getValue());
            return requiredAnnotations.stream()
                    .allMatch(a ->
                            beanDefinitionRegistry
                                    .getBeanDefinitions()
                                    .stream()
                                    .anyMatch(b -> b.getLiteralType().isAnnotationPresent(a)));
        };
    }

    @Bean
    @Primary
    ConditionalOnClassChecker conditionalOnClassConditionChecker() {
        return (annotation, beanDefinition) -> {
            if (annotation == null) return true;
            val classNames = BeanUtil.getConditionalOnClass(annotation);
            return classNames.stream().allMatch(ClassUtil::isClassLoaded);
        };
    }

    @Bean
    @Primary
    ConditionalOnMissingClassChecker conditionalOnMissingClassConditionChecker() {
        return (annotation, beanDefinition) -> {
            if (annotation == null) return true;
            val classNames = BeanUtil.getConditionalOnMissingClass(annotation);
            return classNames.stream().noneMatch(ClassUtil::isClassLoaded);
        };
    }

    @Bean
    @Primary
    ConditionalOnBeanChecker conditionalOnBeanConditionChecker() {
        return (annotation, beanDefinition) -> {
            if (annotation == null) return true;
            val requiredTypes = Arrays.asList(annotation.getValue());
            return requiredTypes
                    .stream()
                    .allMatch(requiredType -> beanDefinitionRegistry
                            .getBeanDefinitions()
                            .stream()
                            .anyMatch(beanDefinitionRegistry.filterBeanDefinition(requiredType)));
        };
    }

    @Bean
    @Primary
    ConditionalOnValueChecker conditionalOnValueConditionChecker(PropertyPostProcessor propertyPostProcessor) {
        return (annotation, beanDefinition) -> {
            if (annotation == null) return true;
            val value = annotation.getValue();
            val expected = annotation.getExpected();
            val result = propertyPostProcessor.process(value.value(), value.source(), String.class);
            return Objects.equals(result, expected);
        };
    }

    @Bean
    @Primary
    ConditionalOnMissingBeanChecker conditionalOnMissingBeanConditionChecker() {
        return (annotation, beanDefinition) -> {
            if (annotation == null || annotation.getValue().length == 0) return true;
            val requiredTypes = Arrays.asList(annotation.getValue());
            return requiredTypes.stream()
                    .noneMatch(requiredType -> beanDefinitionRegistry
                            .getBeanDefinitions()
                            .stream()
                            .filter(b -> !b.getId().equals(beanDefinition.getId()))
                            .anyMatch(beanDefinitionRegistry.filterBeanDefinition(requiredType)));
        };
    }
}