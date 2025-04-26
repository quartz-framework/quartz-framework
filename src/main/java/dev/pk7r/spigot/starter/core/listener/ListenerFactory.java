package dev.pk7r.spigot.starter.core.listener;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface ListenerFactory {

    Plugin getPlugin();

    void registerEvents(Listener listener);

    void unregisterEvents(Listener listener);

}