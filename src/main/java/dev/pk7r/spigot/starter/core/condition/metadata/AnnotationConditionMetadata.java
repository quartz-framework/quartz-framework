package dev.pk7r.spigot.starter.core.condition.metadata;

import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenAnnotationPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationConditionMetadata {

    private Class<? extends Annotation>[] classes;

    public static AnnotationConditionMetadata of(ActivateWhenAnnotationPresent annotation) {
        if (annotation == null) return null;
        return new AnnotationConditionMetadata(annotation.value());
    }
}