package dev.pk7r.spigot.starter.factory.event;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface EventFactory {

    Plugin getPlugin();

    void registerEvents(Listener listener);

}