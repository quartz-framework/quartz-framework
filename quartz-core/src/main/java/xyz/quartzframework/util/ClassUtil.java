package xyz.quartzframework.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ClassUtil {

    public static boolean isClassLoaded(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    public static String extractSimpleName(String typeName) {
        if (typeName == null) return null;
        int lastDot = typeName.lastIndexOf('.');
        return lastDot >= 0 ? typeName.substring(lastDot + 1) : typeName;
    }

    public static String extractPackageName(String typeName) {
        if (typeName == null) return "";
        int lastDot = typeName.lastIndexOf('.');
        return lastDot >= 0 ? typeName.substring(0, lastDot) : "";
    }
}