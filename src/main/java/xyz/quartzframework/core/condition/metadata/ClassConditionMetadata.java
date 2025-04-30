package xyz.quartzframework.core.condition.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import xyz.quartzframework.core.condition.annotation.ActivateWhenClassMissing;
import xyz.quartzframework.core.condition.annotation.ActivateWhenClassPresent;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassConditionMetadata {

    private Set<String> classNames;

    public static ClassConditionMetadata of(ActivateWhenClassPresent annotation) {
        if (annotation == null) return null;
        val classes = Arrays.stream(annotation.value()).map(Class::getName).collect(Collectors.toSet());
        classes.addAll(Arrays.stream(annotation.classNames()).collect(Collectors.toSet()));
        return new ClassConditionMetadata(classes);
    }

    public static ClassConditionMetadata of(ActivateWhenClassMissing annotation) {
        if (annotation == null) return null;
        val classes = Arrays.stream(annotation.value()).map(Class::getName).collect(Collectors.toSet());
        classes.addAll(Arrays.stream(annotation.classNames()).collect(Collectors.toSet()));
        return new ClassConditionMetadata(classes);
    }
}