package dev.pk7r.spigot.starter;

import dev.pk7r.spigot.starter.scanner.ClassScanner;
import io.github.classgraph.ClassGraph;
import lombok.val;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class DefaultClassScanner implements ClassScanner {

    private static ClassScanner classScanner;

    public static ClassScanner getInstance() {
        if (Objects.isNull(classScanner)) {
            classScanner = new DefaultClassScanner();
        }
        return classScanner;
    }

    @Override
    public Set<Class<?>> scan(String[] packages, String[] excludedClasses, Predicate<Class<?>> filter, boolean verbose) {
        val classGraph = new ClassGraph();
        classGraph.verbose(verbose);
        classGraph.acceptPackages(wrapPackages(packages));
        classGraph.enableClassInfo();
        classGraph.rejectClasses(excludedClasses);
        try (val scanResult = classGraph.scan()) {
            return scanResult.getAllClasses().loadClasses().stream().filter(filter).collect(Collectors.toSet());
        } catch (Throwable throwable) {
            throwable.printStackTrace();

        }
        return new HashSet<>();
    }

    private String[] wrapPackages(String[] packages) {
        val wrappedPackages = Arrays.copyOf(packages, packages.length + 1);
        wrappedPackages[packages.length] = "dev.pk7r.spigot.starter";
        return wrappedPackages;
    }
}