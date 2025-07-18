package xyz.quartzframework.beans.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.quartzframework.beans.condition.GenericCondition;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhen;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericConditionMetadata {

    private Class<? extends GenericCondition> value;

    @SuppressWarnings("unchecked")
    public static GenericConditionMetadata of(TypeMetadata metadata) {
        return metadata.getAnnotation(ActivateWhen.class.getName())
                .map(a -> {
                    Class<? extends GenericCondition> clazz = a.getAttribute("value", Class.class);
                    if (clazz == null || !GenericCondition.class.isAssignableFrom(clazz)) return null;
                    return new GenericConditionMetadata(clazz);
                })
                .orElse(null);
    }
}