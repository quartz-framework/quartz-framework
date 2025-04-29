package dev.pk7r.spigot.starter.core.condition.metadata;

import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhen;
import dev.pk7r.spigot.starter.core.condition.GenericCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericConditionMetadata {

    private Class<? extends GenericCondition> value;

    public static GenericConditionMetadata of(ActivateWhen annotation) {
        if (annotation == null) return null;
        return new GenericConditionMetadata(annotation.value());
    }
}