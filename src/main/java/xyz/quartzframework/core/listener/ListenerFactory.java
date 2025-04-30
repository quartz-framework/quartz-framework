package xyz.quartzframework.core.listener;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import xyz.quartzframework.core.annotation.NoProxy;

@NoProxy
public interface ListenerFactory {

    Plugin getPlugin();

    PluginEventExecutor getExecutor();

    void registerEvents(Object bean);

    void unregisterEvents(Listener listener);

}