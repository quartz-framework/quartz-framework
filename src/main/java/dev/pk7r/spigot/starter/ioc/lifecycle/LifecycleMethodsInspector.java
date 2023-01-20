package dev.pk7r.spigot.starter.ioc.lifecycle;

import java.lang.reflect.Method;
import java.util.Set;

public interface LifecycleMethodsInspector {

    Set<Method> getPostConstructMethods();

    Set<Method> getPreDestroyMethods();

    void registerLifecycleMethods(Class<?> clazz);

    void destroyMethods();

    void invokeLifecycleMethod(Method method);

}