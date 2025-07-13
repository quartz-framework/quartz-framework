package xyz.quartzframework.beans.definition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import xyz.quartzframework.config.Property;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMetadata {

    private String value;

    private String source;

    public static PropertyMetadata of(Property annotation) {
        if (annotation == null) return null;
        return new PropertyMetadata(annotation.value(), annotation.source());
    }

    public static PropertyMetadata of(AnnotationMetadata metadata) {
        if (metadata == null || !metadata.getName().equals(Property.class.getName())) {
            return null;
        }
        val value = metadata.getAttribute("value", String.class);
        val source = metadata.getAttribute("source", String.class);
        return new PropertyMetadata(value, source != null ? source : "application");
    }
}