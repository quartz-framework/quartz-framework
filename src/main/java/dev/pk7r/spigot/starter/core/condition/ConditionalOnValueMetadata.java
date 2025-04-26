package dev.pk7r.spigot.starter.core.condition;

import dev.pk7r.spigot.starter.core.annotation.Value;
import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionalOnValueMetadata {

    private Value value;

    private String expected;

    public static ConditionalOnValueMetadata of(ConditionalOnValue annotation) {
        if (annotation == null) return null;
        return new ConditionalOnValueMetadata(annotation.value(), annotation.expected());
    }
}