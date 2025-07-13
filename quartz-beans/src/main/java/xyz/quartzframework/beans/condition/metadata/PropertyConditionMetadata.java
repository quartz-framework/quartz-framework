package xyz.quartzframework.beans.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import xyz.quartzframework.beans.definition.metadata.AnnotationMetadata;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhenPropertyEquals;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyConditionMetadata {

    private AnnotationMetadata property;

    private String expected;

    public static PropertyConditionMetadata of(TypeMetadata metadata) {
        return metadata
                .getAnnotation(ActivateWhenPropertyEquals.class)
                .map(a -> {
                    val propertyAnnotation = a.getAnnotationAttribute("value");
                    val expect = a.getAttribute("expected", String.class);
                    return new PropertyConditionMetadata(propertyAnnotation, expect);
                })
                .orElse(null);
    }
}