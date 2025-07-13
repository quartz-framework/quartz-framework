package xyz.quartzframework.beans.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import xyz.quartzframework.beans.definition.metadata.AnnotationMetadata;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhenPropertyEquals;
import xyz.quartzframework.config.Property;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyConditionMetadata {

    private String expression;

    private String expected;

    private String source;

    public static PropertyConditionMetadata of(TypeMetadata metadata) {
        return metadata
                .getAnnotation(ActivateWhenPropertyEquals.class)
                .map(a -> {
                    val expression = a.getAttribute("property", String.class);
                    val expect = a.getAttribute("expected", String.class);
                    val source = a.getAttribute("source", String.class);
                    return new PropertyConditionMetadata(expression, expect, source);
                })
                .orElse(null);
    }
}