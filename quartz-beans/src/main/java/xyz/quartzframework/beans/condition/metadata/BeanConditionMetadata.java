package xyz.quartzframework.beans.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhenAnnotationPresent;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhenBeanMissing;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhenBeanPresent;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeanConditionMetadata {

    private Set<String> classNames;

    public static BeanConditionMetadata of(ActivateWhenBeanPresent annotation) {
        if (annotation == null) return null;
        val classes = Arrays.stream(annotation.value()).map(Class::getName).collect(Collectors.toSet());
        return new BeanConditionMetadata(classes);
    }

    public static BeanConditionMetadata of(ActivateWhenBeanMissing annotation) {
        if (annotation == null) return null;
        val classes = Arrays.stream(annotation.value()).map(Class::getName).collect(Collectors.toSet());
        return new BeanConditionMetadata(classes);
    }

    public static BeanConditionMetadata of(TypeMetadata metadata, Class<? extends Annotation> conditionType) {
        if (!metadata.hasAnnotation(conditionType)) {
            return null;
        }
        return metadata.getAnnotation(conditionType.getName())
                .map(annotation -> {
                    Class<?>[] value = annotation.getAttribute("value", Class[].class);
                    if (value == null || value.length == 0) {
                        return new BeanConditionMetadata(Set.of());
                    }
                    val classNames = Arrays.stream(value)
                            .map(Class::getName)
                            .collect(Collectors.toSet());
                    return new BeanConditionMetadata(classNames);
                })
                .orElse(null);
    }

    public static BeanConditionMetadata ofPresent(TypeMetadata metadata) {
        return of(metadata, ActivateWhenBeanPresent.class);
    }

    public static BeanConditionMetadata ofMissing(TypeMetadata metadata) {
        return of(metadata, ActivateWhenBeanMissing.class);
    }
}