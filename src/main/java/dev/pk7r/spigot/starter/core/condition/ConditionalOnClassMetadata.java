package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionalOnClassMetadata {

    private String[] classNames;

    private Class<?>[] classes;

    public static ConditionalOnClassMetadata of(ConditionalOnClass annotation) {
        if (annotation == null) return null;
        return new ConditionalOnClassMetadata(annotation.classNames(), annotation.value());
    }
}