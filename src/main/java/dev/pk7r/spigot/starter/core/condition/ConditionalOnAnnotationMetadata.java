package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnAnnotation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionalOnAnnotationMetadata {

    private Class<? extends Annotation>[] value;

    public static ConditionalOnAnnotationMetadata of(ConditionalOnAnnotation annotation) {
        if (annotation == null) return null;
        return new ConditionalOnAnnotationMetadata(annotation.value());
    }
}