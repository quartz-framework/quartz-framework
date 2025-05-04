package xyz.quartzframework.core.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.quartzframework.core.condition.annotation.ActivateWhenPropertyEquals;
import xyz.quartzframework.core.property.Property;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyConditionMetadata {

    private Property property;

    private String expected;

    public static PropertyConditionMetadata of(ActivateWhenPropertyEquals annotation) {
        if (annotation == null) return null;
        return new PropertyConditionMetadata(annotation.value(), annotation.expected());
    }
}