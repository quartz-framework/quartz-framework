package dev.pk7r.spigot.starter.core.factory;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface EventFactory {

    Plugin getPlugin();

    void registerEvents(Listener listener);

}