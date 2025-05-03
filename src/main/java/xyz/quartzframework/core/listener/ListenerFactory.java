package xyz.quartzframework.core.listener;

import xyz.quartzframework.core.annotation.NoProxy;

@NoProxy
public interface ListenerFactory<T, E> {

    PluginEventExecutor<T, E> getExecutor();

    void registerEvents(Object bean);

    void unregisterEvents(Object listener);

}