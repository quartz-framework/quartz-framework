package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnMissingBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionalOnMissingBeanMetadata {

    private Class<?>[] value;

    public static ConditionalOnMissingBeanMetadata of(ConditionalOnMissingBean annotation) {
        if (annotation == null) return null;
        return new ConditionalOnMissingBeanMetadata(annotation.value());
    }
}