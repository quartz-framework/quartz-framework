package xyz.quartzframework.core.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import xyz.quartzframework.core.bean.definition.metadata.TypeMetadata;
import xyz.quartzframework.core.condition.annotation.ActivateWhenPropertyEquals;
import xyz.quartzframework.core.property.Property;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyConditionMetadata {

    private Property property;

    private String expected;

    public static PropertyConditionMetadata of(TypeMetadata metadata) {
        return metadata
                .getAnnotation(ActivateWhenPropertyEquals.class)
                .map(a -> {
                    val p = a.getAttribute("property", Property.class);
                    val expect = a.getAttribute("expected", String.class);
                    return new PropertyConditionMetadata(p, expect);
                })
                .orElse(null);
    }
}