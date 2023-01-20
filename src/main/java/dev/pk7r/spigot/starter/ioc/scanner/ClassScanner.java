package dev.pk7r.spigot.starter.ioc.scanner;

import java.util.Set;
import java.util.function.Predicate;

public interface ClassScanner {

    Set<Class<?>> scan(String[] packages, String[] excludedClasses, Predicate<Class<?>> filter, boolean verbose);

}