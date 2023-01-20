package dev.pk7r.spigot.starter.ioc.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

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