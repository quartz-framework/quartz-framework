package xyz.quartzframework.core.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.quartzframework.core.condition.GenericCondition;
import xyz.quartzframework.core.condition.annotation.ActivateWhen;

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