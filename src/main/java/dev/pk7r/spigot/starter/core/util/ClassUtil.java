package dev.pk7r.spigot.starter.core.util;

import io.github.classgraph.ClassGraph;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException exception) {
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
        wrappedPackages[packages.length] = "dev.pk7r.spigot.starter.core";
        return wrappedPackages;
    }
}