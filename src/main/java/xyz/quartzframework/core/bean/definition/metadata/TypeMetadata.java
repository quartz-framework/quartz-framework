package xyz.quartzframework.core.bean.definition.metadata;

import io.github.classgraph.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import xyz.quartzframework.core.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static xyz.quartzframework.core.util.ClassUtil.extractPackageName;
import static xyz.quartzframework.core.util.ClassUtil.extractSimpleName;

@Slf4j
@Getter
@ToString
@AllArgsConstructor(staticName = "of")
@Builder
public class TypeMetadata {

    private Class<?> clazz;

    private final URLClassLoader classLoader;

    @NonNull
    private final String fullName;

    @NonNull
    private final String simpleName;

    @NonNull
    private final String packageName;

    @NonNull
    @Builder.Default
    private final List<ParameterMetadata> parameters = Collections.emptyList();

    @Builder.Default
    private final List<AnnotationMetadata> annotations = Collections.emptyList();

    @Builder.Default
    private final List<MethodMetadata> methods = Collections.emptyList();

    @Builder.Default
    private final List<TypeMetadata> superTypes = Collections.emptyList();

    @Builder.Default
    private final List<TypeMetadata> assignableTypes = Collections.emptyList();

    private final boolean raw;

    private final boolean annotation;

    private final int modifiers;

    private final String declaredByClass;

    private final String declaredByMethod;

    public static TypeMetadata of(ClassInfo classInfo, URLClassLoader classLoader) {
        val annInfo = classInfo.getAnnotationInfo();
        val allAnnotations = annInfo != null
                ? annInfo.stream().filter(Objects::nonNull).flatMap(a -> AnnotationMetadata.resolve(a, classLoader, new HashSet<>()).stream()).toList()
                : List.<AnnotationMetadata>of();
        val methods = classInfo
                .getMethodInfo()
                .stream()
                .map(m -> MethodMetadata.of(m, classLoader))
                .toList();
        val parameterMetadata = new ArrayList<ParameterMetadata>();
        for (TypeParameter typeParam : classInfo.getTypeSignatureOrTypeDescriptor().getTypeParameters()) {
            parameterMetadata.add(ParameterMetadata.of(typeParam));
        }
        val superTypes = new ArrayList<TypeMetadata>();
        val assignableTypes = new ArrayList<TypeMetadata>();
        val superClass = classInfo.getSuperclass();
        if (superClass != null) {
            superTypes.add(TypeMetadata.of(superClass, classLoader));
            assignableTypes.add(TypeMetadata.of(superClass, classLoader));
        }
        for (val iface : classInfo.getInterfaces()) {
            superTypes.add(TypeMetadata.of(iface, classLoader));
            assignableTypes.add(TypeMetadata.of(iface, classLoader));
        }
        for (val info : classInfo.getSuperclasses()) {
            assignableTypes.add(TypeMetadata.of(info, classLoader));
        }
        return TypeMetadata.builder()
                .simpleName(classInfo.getSimpleName())
                .methods(methods)
                .raw(false)
                .classLoader(classLoader)
                .assignableTypes(assignableTypes)
                .superTypes(superTypes)
                .fullName(classInfo.getName())
                .annotations(allAnnotations)
                .annotation(classInfo.isAnnotation())
                .parameters(parameterMetadata)
                .modifiers(classInfo.getModifiers())
                .declaredByClass(classInfo.getName())
                .packageName(classInfo.getPackageName())
                .build();
    }

    public static TypeMetadata of(MethodMetadata methodMetadata) {
        val returnType = methodMetadata.getReturnType();
        return TypeMetadata.builder()
                .simpleName(returnType.getSimpleName())
                .raw(false)
                .fullName(returnType.getFullName())
                .packageName(returnType.getPackageName())
                .parameters(returnType.getParameters())
                .superTypes(returnType.getSuperTypes())
                .assignableTypes(returnType.getAssignableTypes())
                .annotations(methodMetadata.getAnnotations())
                .classLoader(methodMetadata.getClassLoader())
                .annotation(returnType.isAnnotation())
                .modifiers(methodMetadata.getModifiers())
                .declaredByClass(methodMetadata.getDeclaredByClass())
                .declaredByMethod(methodMetadata.getName())
                .methods(List.of(methodMetadata))
                .build();
    }

    public static TypeMetadata of(Class<?> clazz, URLClassLoader classLoader) {
        List<MethodMetadata> methods = new ArrayList<>(Arrays.stream(clazz.getDeclaredMethods()).map(m -> MethodMetadata.of(m, classLoader)).toList());
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
            val meta = TypeMetadata.of(sc, classLoader);
            superT.add(meta);
            assignable.add(meta);
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            val meta = TypeMetadata.of(iface, classLoader);
            superT.add(meta);
            assignable.add(meta);
        }
        return TypeMetadata.builder()
                .clazz(clazz)
                .raw(false)
                .assignableTypes(assignable)
                .superTypes(superT)
                .annotation(clazz.isAnnotation())
                .fullName(clazz.getName())
                .simpleName(clazz.getSimpleName())
                .modifiers(clazz.getModifiers())
                .classLoader(classLoader)
                .methods(methods)
                .packageName(clazz.getPackage() != null ? clazz.getPackage().getName() : extractPackageName(clazz.getName()))
                .declaredByClass(clazz.getName())
                .annotations(annotations)
                .parameters(parameterMetadata)
                .build();
    }

    public static TypeMetadata raw(Class<?> clazz, URLClassLoader loader) {
        val annotations = Arrays
                .stream(clazz.getAnnotations())
                .flatMap(a -> AnnotationMetadata.resolve(a, loader, new HashSet<>()).stream())
                .toList();
        val assignable = new ArrayList<TypeMetadata>();
        val superT = new ArrayList<TypeMetadata>();

        val sc = clazz.getSuperclass();
        if (sc != null && sc != Object.class) {
            val meta = TypeMetadata.of(sc, loader);
            superT.add(meta);
            assignable.add(meta);
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            val meta = TypeMetadata.of(iface, loader);
            superT.add(meta);
            assignable.add(meta);
        }
        return TypeMetadata.builder()
                .clazz(clazz)
                .raw(true)
                .classLoader(loader)
                .assignableTypes(assignable)
                .superTypes(superT)
                .annotation(clazz.isAnnotation())
                .fullName(clazz.getName())
                .simpleName(clazz.getSimpleName())
                .packageName(clazz.getPackage() != null ? clazz.getPackage().getName() : extractPackageName(clazz.getName()))
                .modifiers(clazz.getModifiers())
                .annotations(annotations)
                .parameters(List.of())
                .methods(List.of())
                .declaredByClass(clazz.getName())
                .build();
    }

    private static List<ParameterMetadata> fromTypeSignatureGenerics(HierarchicalTypeSignature signature, URLClassLoader classLoader) {

        if (signature instanceof ClassRefTypeSignature crts) {
            return crts.getTypeArguments()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(ParameterMetadata::of)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static TypeMetadata fromTypeSignature(HierarchicalTypeSignature signature, URLClassLoader classLoader) {
        if (signature == null) return null;
        val typeName = signature.toString();
        val parameters = fromTypeSignatureGenerics(signature, classLoader);
        val annInfo = signature.getTypeAnnotationInfo();
        val allAnnotations = annInfo != null
                ? annInfo.stream().filter(Objects::nonNull).flatMap(a -> AnnotationMetadata.resolve(a, classLoader, new HashSet<>()).stream()).toList()
                : List.<AnnotationMetadata>of();
        return TypeMetadata
                .builder()
                .fullName(typeName)
                .annotation(false)
                .classLoader(classLoader)
                .simpleName(extractSimpleName(typeName))
                .packageName(extractPackageName(typeName))
                .parameters(parameters)
                .annotations(allAnnotations)
                .build();
    }

    public boolean hasAnnotation(String annotationName) {
        return annotations.stream().anyMatch(a -> a.getName().equals(annotationName));
    }

    public Optional<AnnotationMetadata> getAnnotation(String annotationName) {
        return annotations.stream().filter(a -> a.getName().equals(annotationName)).findFirst();
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return hasAnnotation(annotationType.getName());
    }

    public Optional<AnnotationMetadata> getAnnotation(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType.getName());
    }

    public String getRawName() {
        return fullName;
    }

    public String getFullName() {
        if (parameters.isEmpty()) return fullName;
        return fullName + parameters.stream()
                .map(ParameterMetadata::getFullName)
                .collect(Collectors.joining(", ", "<", ">"));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TypeMetadata that = (TypeMetadata) obj;
        val compareRaw = this.raw || that.raw;
        val thisName = compareRaw ? this.getRawName() : this.getFullName();
        val thatName = compareRaw ? that.getRawName() : that.getFullName();
        return modifiers == that.modifiers &&
                Objects.equals(thisName, thatName) &&
                Objects.equals(annotations, that.annotations) &&
                Objects.equals(declaredByClass, that.declaredByClass) &&
                Objects.equals(declaredByMethod, that.declaredByMethod);
    }

    @Override
    public int hashCode() {
        val name = raw ? getRawName() : getFullName();
        return Objects.hash(name, annotations, modifiers, declaredByClass, declaredByMethod);
    }

    public boolean isMethodBean() {
        return declaredByMethod != null;
    }

    public boolean isClassBean() {
        return declaredByMethod == null;
    }

    public boolean isClassLoaded() {
        return clazz != null;
    }

    @SneakyThrows
    public Class<?> getType() {
        if (clazz != null) return clazz;
        val isLoaded = ClassUtil.isClassLoaded(getFullName(), classLoader);
        clazz = Class.forName(getFullName(), !isLoaded, classLoader);
        return clazz;
    }

    public static Set<TypeMetadata> scan(String[] packages, String[] excludedClasses, Predicate<ClassInfo> infoFilter, Predicate<TypeMetadata> filter, boolean verbose, URLClassLoader classLoader) {
        val classGraph = new ClassGraph();
        classGraph.verbose(verbose);
        classGraph.acceptPackages(wrapPackages(packages));
        classGraph.rejectClasses(excludedClasses);
        classGraph.enableAllInfo();
        try (val scanResult = classGraph.scan()) {
            return scanResult
                    .getAllClasses()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(infoFilter)
                    .map(c -> TypeMetadata.of(c, classLoader))
                    .filter(filter)
                    .collect(Collectors.toSet());
        } catch (Throwable throwable) {
            log.error("Failed to scan classes: ", throwable);
        }
        return new HashSet<>();
    }

    private static String[] wrapPackages(String[] packages) {
        val wrappedPackages = Arrays.copyOf(packages, packages.length + 1);
        wrappedPackages[packages.length] = "%s.*".formatted(INTERNAL_PACKAGE);
        return wrappedPackages;
    }

    public Map<String, MethodMetadata> getMethodMap() {
        return this.methods.stream().collect(Collectors.toMap(MethodMetadata::getName, m -> m));
    }

    public boolean matches(TypeMetadata other) {
        if (other == null) return false;
        if (this.equals(other)) return true;
        if ((this.raw || other.raw) && this.getRawName().equals(other.getRawName())) return true;
        return this.isAssignableTo(other);
    }

    public boolean isAssignableTo(TypeMetadata target) {
        if (target == null) return false;
        if (getFullName().equals(target.getFullName())) return true;
        return assignableTypes
                .stream()
                .anyMatch(candidate -> candidate.getRawName().equals(target.getRawName())
                        || candidate.isAssignableTo(target));
    }

    public static final String INTERNAL_PACKAGE = "xyz.quartzframework";
}