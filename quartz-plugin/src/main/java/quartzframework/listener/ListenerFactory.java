package quartzframework.listener;


import xyz.quartzframework.aop.NoProxy;

@NoProxy
public interface ListenerFactory<T, E> {

    QuartzEventExecutor<T, E> getExecutor();

    void registerEvents(Object bean);

    void unregisterEvents(Object listener);

}