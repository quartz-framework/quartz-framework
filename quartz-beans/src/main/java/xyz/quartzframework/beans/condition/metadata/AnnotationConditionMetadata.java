package xyz.quartzframework.beans.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhenAnnotationPresent;

import java.lang.annotation.Annotation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationConditionMetadata {

    private Class<? extends Annotation>[] classes;

    public static AnnotationConditionMetadata of(TypeMetadata metadata) {
        val value = metadata
                .getAnnotation(ActivateWhenAnnotationPresent.class)
                .map(a -> a.getAttribute("value", Class[].class))
                .orElse(new Class[0]);
        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] result = (Class<? extends Annotation>[]) value;
        return new AnnotationConditionMetadata(result);
    }
}