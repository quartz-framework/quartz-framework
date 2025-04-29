package dev.pk7r.spigot.starter.core.condition.metadata;

import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenBeanMissing;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenBeanPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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