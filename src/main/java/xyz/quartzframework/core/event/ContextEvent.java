package xyz.quartzframework.core.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import xyz.quartzframework.core.context.AbstractPluginContext;

@Getter
@RequiredArgsConstructor
public abstract class ContextEvent extends Event {

    private final AbstractPluginContext context;

}
