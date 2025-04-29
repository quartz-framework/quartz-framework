package dev.pk7r.spigot.starter.core.listener;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@NoProxy
public interface ListenerFactory {

    Plugin getPlugin();

    PluginEventExecutor getExecutor();

    void registerEvents(Object bean);

    void unregisterEvents(Listener listener);

}