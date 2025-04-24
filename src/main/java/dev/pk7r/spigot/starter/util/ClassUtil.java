package dev.pk7r.spigot.starter.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassUtil {

    public boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}