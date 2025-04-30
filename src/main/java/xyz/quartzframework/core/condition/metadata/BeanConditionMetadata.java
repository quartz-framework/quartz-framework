package xyz.quartzframework.core.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanPresent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeanConditionMetadata {

    private Class<?>[] classes;

    public static BeanConditionMetadata of(ActivateWhenBeanPresent annotation) {
        if (annotation == null) return null;
        return new BeanConditionMetadata(annotation.value());
    }

    public static BeanConditionMetadata of(ActivateWhenBeanMissing annotation) {
        if (annotation == null) return null;
        return new BeanConditionMetadata(annotation.value());
    }
}