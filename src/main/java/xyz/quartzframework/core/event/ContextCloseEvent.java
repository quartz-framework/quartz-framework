package xyz.quartzframework.core.event;

import org.bukkit.event.HandlerList;
import xyz.quartzframework.core.context.AbstractPluginContext;

public class ContextCloseEvent extends ContextEvent {

    private static final HandlerList handlers = new HandlerList();

    public ContextCloseEvent(AbstractPluginContext context) {
        super(context);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}