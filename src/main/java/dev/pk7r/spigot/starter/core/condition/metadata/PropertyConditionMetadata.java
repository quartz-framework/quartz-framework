package dev.pk7r.spigot.starter.core.condition.metadata;

import dev.pk7r.spigot.starter.core.annotation.Property;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenPropertyEquals;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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