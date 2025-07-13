package quartzframework.event;

public interface EventPublisher {

    default void publish(Object event) {
        publish(event, false);
    }

    default void publish(Object event, boolean async) {
        publish(event, false, async);
    }

    void publish(Object event, boolean internal, boolean async);

}