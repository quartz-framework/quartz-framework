package xyz.quartzframework.beans.definition.metadata;

import io.github.classgraph.MethodInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLClassLoader;
import java.util.*;

import static xyz.quartzframework.util.ClassUtil.extractPackageName;

@Slf4j
@Data
@Builder
public class MethodMetadata {

    private final String name;

    private final URLClassLoader classLoader;

    private final int modifiers;

    private final String declaredByClass;

    private final TypeMetadata returnType;

    @Singular("parameter")
    private final List<ParameterMetadata> parameters;

    @Singular("annotation")
    private final List<AnnotationMetadata> annotations;

    public static MethodMetadata of(Method method, URLClassLoader classLoader) {
        val re = method.getReturnType();
        val returnType = returnType(re, classLoader);
        val parameters = Arrays.stream(method.getGenericParameterTypes())
                .filter(t -> t instanceof Class<?>)
                .map(t -> ParameterMetadata.of((Class<?>) t))
                .toList();
        val annotations = Arrays.stream(method.getAnnotations())
                .flatMap(a -> AnnotationMetadata.resolve(a, classLoader, new HashSet<>()).stream())
                .toList();
        return MethodMetadata.builder()
                .classLoader(classLoader)
                .name(method.getName())
                .modifiers(method.getModifiers())
                .declaredByClass(method.getDeclaringClass().getName())
                .returnType(returnType)
                .parameters(parameters)
                .annotations(annotations)
                .build();
    }

    public static MethodMetadata of(MethodInfo methodInfo, URLClassLoader classLoader) {
        val signature = methodInfo.getTypeSignatureOrTypeDescriptor();
        val returnType = TypeMetadata.fromTypeSignature(signature.getResultType(), classLoader);
        val parameters = signature.getTypeParameters()
                .stream()
                .map(ParameterMetadata::of)
                .filter(Objects::nonNull)
                .toList();
        val annInfo = methodInfo.getAnnotationInfo();
        val allAnnotations = annInfo != null
                ? annInfo.stream().filter(Objects::nonNull).flatMap(a -> AnnotationMetadata.resolve(a, classLoader, new HashSet<>()).stream()).toList()
                : List.<AnnotationMetadata>of();
        return MethodMetadata.builder()
                .name(methodInfo.getName())
                .classLoader(classLoader)
                .modifiers(methodInfo.getModifiers())
                .declaredByClass(methodInfo.getClassInfo().getName())
                .returnType(returnType)
                .parameters(parameters)
                .annotations(allAnnotations)
                .build();
    }

    public boolean hasAnnotation(String annotationName) {
        return annotations.stream().anyMatch(a -> a.getName().equals(annotationName));
    }

    public boolean hasAnnotation(Class<?> annotationType) {
        return hasAnnotation(annotationType.getName());
    }

    public Optional<AnnotationMetadata> getAnnotation(String annotationName) {
        return annotations.stream().filter(a -> a.getName().equals(annotationName)).findFirst();
    }

    public Optional<AnnotationMetadata> getAnnotation(Class<?> annotationType) {
        return getAnnotation(annotationType.getName());
    }

    public boolean isVoid() {
        return returnType != null && "void".equals(returnType.getSimpleName());
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public boolean isPublic() {
        return java.lang.reflect.Modifier.isPublic(modifiers);
    }

    public boolean isStatic() {
        return java.lang.reflect.Modifier.isStatic(modifiers);
    }

    public boolean isPrivate() {
        return java.lang.reflect.Modifier.isPrivate(modifiers);
    }

    public boolean isProtected() {
        return java.lang.reflect.Modifier.isProtected(modifiers);
    }

    public boolean isPackagePrivate() {
        return !isPublic() && !isPrivate() && !isProtected();
    }

    public boolean isSynthetic() {
        return name.contains("lambda$") || name.contains("$");
    }

    @SneakyThrows
    public Method getMethod() {
        val clazz = Class.forName(declaredByClass, true, getClassLoader());
        val all = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(name))
                .findFirst();
        return all.orElseThrow(() -> new NoSuchMethodException("Method not found: " + declaredByClass + "#" + name));
    }

    @Override
    public String toString() {
        return "%s %s(%s)".formatted(
                returnType.getSimpleName(),
                name,
                String.join(", ", parameters.stream().map(ParameterMetadata::getFullName).toList())
        );
    }

    private static TypeMetadata returnType(Class<?> clazz, URLClassLoader classLoader) {
        val annotations = Arrays
                .stream(clazz.getAnnotations())
                .flatMap(a -> AnnotationMetadata.resolve(a, classLoader, new HashSet<>()).stream())
                .toList();
        val parameterMetadata = new ArrayList<ParameterMetadata>();
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType pt) {
                for (Type arg : pt.getActualTypeArguments()) {
                    if (arg instanceof Class<?> cls) {
                        parameterMetadata.add(ParameterMetadata.of(cls));
                    }
                }
            }
        }
        val superclass = clazz.getGenericSuperclass();
        if (superclass instanceof ParameterizedType pt) {
            for (Type arg : pt.getActualTypeArguments()) {
                if (arg instanceof Class<?> cls) {
                    parameterMetadata.add(ParameterMetadata.of(cls));
                }
            }
        }
        val assignable = new ArrayList<TypeMetadata>();
        val superT = new ArrayList<TypeMetadata>();

        val sc = clazz.getSuperclass();
        if (sc != null && sc != Object.class) {
            val meta = returnType(sc, classLoader);
            superT.add(meta);
            assignable.add(meta);
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            val meta = returnType(iface, classLoader);
            superT.add(meta);
            assignable.add(meta);
        }

        return TypeMetadata
                .builder()
                .clazz(clazz)
                .assignableTypes(assignable)
                .superTypes(superT)
                .annotation(clazz.isAnnotation())
                .fullName(clazz.getName())
                .simpleName(clazz.getSimpleName())
                .modifiers(clazz.getModifiers())
                .classLoader(classLoader)
                .methods(List.of())
                .packageName(clazz.getPackage() != null ? clazz.getPackage().getName() : extractPackageName(clazz.getName()))
                .declaredByClass(clazz.getName())
                .annotations(annotations)
                .parameters(parameterMetadata)
                .build();
    }
}