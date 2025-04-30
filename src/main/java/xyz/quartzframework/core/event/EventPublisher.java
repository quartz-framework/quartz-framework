package xyz.quartzframework.core.event;

import org.bukkit.event.Event;

public interface EventPublisher {

    default void publish(Event event) {
        publish(event, false);
    }

    default void publish(Event event, boolean async) {
        publish(event, false, async);
    }

    void publish(Event event, boolean internal, boolean async);

}