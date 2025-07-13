package xyz.quartzframework.beans.definition.metadata;

import io.github.classgraph.*;
import lombok.*;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class AnnotationMetadata {

    private URLClassLoader classLoader;

    private String name;

    private Map<String, Object> attributes;

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) return null;
        if (type.isPrimitive()) {
            if (type == int.class && value instanceof Integer i) return (T) i;
            if (type == long.class && value instanceof Long l) return (T) l;
            if (type == short.class && value instanceof Short s) return (T) s;
            if (type == byte.class && value instanceof Byte b) return (T) b;
            if (type == double.class && value instanceof Double d) return (T) d;
            if (type == float.class && value instanceof Float f) return (T) f;
            if (type == char.class && value instanceof Character c) return (T) c;
            if (type == boolean.class && value instanceof Boolean b) return (T) b;
            throw new IllegalArgumentException("Cannot cast annotation attribute '%s' to primitive %s (was %s: %s)"
                    .formatted(key, type.getName(), value.getClass().getName(), value));
        }
        if (type == Class[].class) {
            if (value instanceof Class[]) return (T) value;
            if (value instanceof String s) return (T) new Class[]{ resolveClass(s) };
            if (value instanceof String[] sa)
                return (T) Arrays.stream(sa).map(this::resolveClass).toArray(Class[]::new);
            if (value instanceof Object[] arr)
                return (T) Arrays.stream(arr).map(this::resolveToClass).toArray(Class[]::new);
        }
        if (type.isEnum() && value instanceof String s) {
            return (T) Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), s);
        }
        try {
            return type.cast(value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Cannot cast annotation attribute '%s' to %s (was %s: %s)"
                    .formatted(key, type.getName(), value.getClass().getName(), value), e);
        }
    }

    private Class<?> resolveClass(String s) {
        if (s.endsWith(".class")) s = s.substring(0, s.length() - 6);
        try {
            return Class.forName(s, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not resolve class: " + s, e);
        }
    }

    private Class<?> resolveToClass(Object o) {
        if (o instanceof Class<?> c) return c;
        if (o instanceof String s) return resolveClass(s);
        if (o instanceof AnnotationClassRef acr) return resolveClass(acr.getName());
        throw new IllegalArgumentException("Cannot convert to Class: " + o.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public static AnnotationMetadata of(AnnotationInfo info, URLClassLoader classLoader) {
        val rawAttributes = info.getParameterValues().stream()
                .collect(Collectors.toMap(
                        AnnotationParameterValue::getName,
                        AnnotationParameterValue::getValue
                ));
        Map<String, Object> aliasedAttributes = new HashMap<>(rawAttributes);
        try {
            Class<?> annotationClass = Class.forName(info.getName());
            if (Annotation.class.isAssignableFrom(annotationClass)) {
                val aliasMap = resolveAliases((Class<? extends Annotation>) annotationClass);
                for (Map.Entry<String, String> aliasEntry : aliasMap.entrySet()) {
                    String source = aliasEntry.getKey();
                    String target = aliasEntry.getValue();
                    if (rawAttributes.containsKey(source) && rawAttributes.containsKey(target)) {
                        Object sourceVal = rawAttributes.get(source);
                        Object targetVal = rawAttributes.get(target);
                        if (!Objects.equals(sourceVal, targetVal)) {
                            throw new IllegalStateException("Conflicting values for alias attributes: " + source + " and " + target);
                        }
                    }
                    if (rawAttributes.containsKey(source) && !aliasedAttributes.containsKey(target)) {
                        aliasedAttributes.put(target, rawAttributes.get(source));
                    } else if (rawAttributes.containsKey(target) && !aliasedAttributes.containsKey(source)) {
                        aliasedAttributes.put(source, rawAttributes.get(target));
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {}
        return AnnotationMetadata
                .builder()
                .name(info.getName())
                .classLoader(classLoader)
                .attributes(aliasedAttributes)
                .build();
    }

    public static AnnotationMetadata of(Annotation annotation, URLClassLoader classLoader) {
        val annotationType = annotation.annotationType();
        val aliasMap = resolveAliases(annotationType);
        val rawAttributes = new HashMap<String, Object>();
        for (Method method : annotationType.getDeclaredMethods()) {
            try {
                Object value = method.invoke(annotation);
                rawAttributes.put(method.getName(), value);
                String aliasFor = aliasMap.get(method.getName());
                if (aliasFor != null && !rawAttributes.containsKey(aliasFor)) {
                    rawAttributes.put(aliasFor, value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read annotation attribute: " + method.getName(), e);
            }
        }
        return AnnotationMetadata
                .builder()
                .name(annotationType.getName())
                .attributes(rawAttributes)
                .classLoader(classLoader)
                .build();
    }

    public static List<AnnotationMetadata> resolve(AnnotationInfo annotationInfo, URLClassLoader classLoader, Set<String> visited) {
        if (visited.contains(annotationInfo.getName())) return List.of();
        visited.add(annotationInfo.getName());

        val current = AnnotationMetadata.of(annotationInfo, classLoader);
        val metadataList = new ArrayList<AnnotationMetadata>();
        metadataList.add(current);

        val annotationClassInfo = annotationInfo.getClassInfo();
        if (annotationClassInfo != null) {
            val metaAnnotations = annotationClassInfo.getAnnotationInfo();
            for (val meta : metaAnnotations) {
                if (!isDeclaredOnlyAsAttribute(current, meta)) {
                    metadataList.addAll(resolve(meta, classLoader, visited));
                }
            }
        }
        return metadataList;
    }

    public static List<AnnotationMetadata> resolve(Annotation annotation, URLClassLoader classLoader, Set<String> visited) {
        val annotationType = annotation.annotationType();
        val fqName = annotationType.getName();
        if (!visited.add(fqName)) return List.of();
        val current = of(annotation, classLoader);
        val metas = Arrays.stream(annotationType.getAnnotations())
                .filter(a -> !isDeclaredOnlyAsAttribute(current, a.annotationType().getName()))
                .flatMap(a -> resolve(a, classLoader, visited).stream())
                .toList();
        val combined = new ArrayList<AnnotationMetadata>();
        combined.add(current);
        combined.addAll(metas);
        return combined;
    }

    private static boolean isDeclaredOnlyAsAttribute(AnnotationMetadata current, AnnotationInfo candidate) {
        return current.getAttributes()
                .values()
                .stream()
                .flatMap(AnnotationMetadata::flattenPossibleAnnotationClassRefs)
                .anyMatch(name -> name.equals(candidate.getName()));
    }

    private static boolean isDeclaredOnlyAsAttribute(AnnotationMetadata current, String candidateName) {
        return current.getAttributes()
                .values()
                .stream()
                .flatMap(AnnotationMetadata::flattenPossibleAnnotationClassRefs)
                .anyMatch(name -> name.equals(candidateName));
    }

    private static Stream<String> flattenPossibleAnnotationClassRefs(Object val) {
        if (val instanceof Class<?> cls) return Stream.of(cls.getName());
        if (val instanceof String s && s.endsWith(".class")) return Stream.of(s.replace(".class", ""));
        if (val instanceof AnnotationClassRef acr) return Stream.of(acr.getName());
        if (val instanceof Object[] arr) {
            return Arrays.stream(arr)
                    .flatMap(o -> flattenPossibleAnnotationClassRefs(o).distinct());
        }
        return Stream.empty();
    }

    private static Map<String, String> resolveAliases(Class<? extends Annotation> annotationType) {
        Map<String, String> aliasMap = new HashMap<>();
        for (Method method : annotationType.getDeclaredMethods()) {
            AliasFor alias = method.getAnnotation(AliasFor.class);
            if (alias != null) {
                String target = alias.attribute();
                if (target.isEmpty()) target = alias.value();
                if (!target.isEmpty() && !target.equals(method.getName())) {
                    aliasMap.put(method.getName(), target);
                }
            }
        }
        return aliasMap;
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> A toReflectiveAnnotation(ClassLoader classLoader, Class<A> annotationType) {
        try {
            Class<?> type = Class.forName(annotationType.getName(), false, classLoader);
            for (Annotation ann : type.getAnnotations()) {
                if (ann.annotationType().getName().equals(name)) {
                    return (A) ann;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}