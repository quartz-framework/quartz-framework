package xyz.quartzframework.core.listener;

import java.lang.reflect.Method;

public interface PluginEventExecutor<T, E> {

    T create(Object bean, Method method);

    void triggerEvent(Object bean, Method method, E event);

}