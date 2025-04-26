package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnMissingClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionalOnMissingClassMetadata {

    private String[] classNames;

    private Class<?>[] classes;

    public static ConditionalOnMissingClassMetadata of(ConditionalOnMissingClass annotation) {
        if (annotation == null) return null;
        return new ConditionalOnMissingClassMetadata(annotation.classNames(), annotation.value());
    }
}