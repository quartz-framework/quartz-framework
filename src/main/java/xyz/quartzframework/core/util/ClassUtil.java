package xyz.quartzframework.core.util;

import io.github.classgraph.ClassGraph;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class ClassUtil {

    public boolean isClassLoaded(String className, ClassLoader classLoader) {
        try {
            val clazz = Class.forName(className, false, classLoader);
            return isAllDependenciesResolved(clazz);
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    public boolean isAllDependenciesResolved(Class<?> clazz) {
        try {
            for (Method method : clazz.getDeclaredMethods()) {
                method.getReturnType();
                method.getParameterTypes();
            }
            for (var constructor : clazz.getDeclaredConstructors()) {
                constructor.getParameterTypes();
            }
            for (var field : clazz.getDeclaredFields()) {
                field.getType();
            }
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public Set<Class<?>> scan(String[] packages, String[] excludedClasses, Predicate<Class<?>> filter, boolean verbose) {
        val classGraph = new ClassGraph();
        classGraph.verbose(verbose);
        classGraph.acceptPackages(wrapPackages(packages));
        classGraph.enableClassInfo();
        classGraph.rejectClasses(excludedClasses);
        try (val scanResult = classGraph.scan()) {
            return scanResult
                    .getAllClasses()
                    .stream()
                    .map(classInfo -> {
                        try {
                            return classInfo.loadClass();
                        } catch (Throwable ignored) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(filter)
                    .collect(Collectors.toSet());
        } catch (Throwable throwable) {
            log.error("Failed to scan classes: ", throwable);
        }
        return new HashSet<>();
    }

    private String[] wrapPackages(String[] packages) {
        val wrappedPackages = Arrays.copyOf(packages, packages.length + 1);
        wrappedPackages[packages.length] = "%s.*".formatted(INTERNAL_PACKAGE);
        return wrappedPackages;
    }

    public static final String INTERNAL_PACKAGE = "xyz.quartzframework";
}