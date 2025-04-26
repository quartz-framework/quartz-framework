package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionalOnBeanMetadata {

    private Class<?>[] value;

    public static ConditionalOnBeanMetadata of(ConditionalOnBean annotation) {
        if (annotation == null) return null;
        return new ConditionalOnBeanMetadata(annotation.value());
    }
}