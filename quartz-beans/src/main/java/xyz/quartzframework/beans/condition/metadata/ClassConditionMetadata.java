package xyz.quartzframework.beans.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhenClassMissing;
import xyz.quartzframework.beans.support.annotation.condition.ActivateWhenClassPresent;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassConditionMetadata {

    private Set<String> classNames;

    public static ClassConditionMetadata of(TypeMetadata metadata, Class<? extends Annotation> conditionType) {
        if (!metadata.hasAnnotation(conditionType)) {
            return null;
        }
        return metadata.getAnnotation(conditionType.getName())
                .map(annotation -> {
                    Set<String> result = new HashSet<>();
                    val classes = annotation.getAttribute("value", Class[].class);
                    if (classes != null) {
                        Arrays.stream(classes).map(Class::getName).forEach(result::add);
                    }
                    val classNames = annotation.getAttribute("classNames", String[].class);
                    if (classNames != null) {
                        result.addAll(Arrays.asList(classNames));
                    }
                    return new ClassConditionMetadata(result);
                })
                .orElse(null);
    }

    public static ClassConditionMetadata ofPresent(TypeMetadata metadata) {
        return of(metadata, ActivateWhenClassPresent.class);
    }

    public static ClassConditionMetadata ofMissing(TypeMetadata metadata) {
        return of(metadata, ActivateWhenClassMissing.class);
    }
}