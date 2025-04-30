package xyz.quartzframework.core.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.quartzframework.core.condition.annotation.ActivateWhenAnnotationPresent;

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