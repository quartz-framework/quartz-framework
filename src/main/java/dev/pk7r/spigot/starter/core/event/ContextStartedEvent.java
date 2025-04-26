package dev.pk7r.spigot.starter.core.event;

import dev.pk7r.spigot.starter.core.context.PluginContext;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ContextStartedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final PluginContext context;

    public ContextStartedEvent(PluginContext context) {
        this.context = context;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}