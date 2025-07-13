package quartzframework.listener;

import java.lang.reflect.Method;

public interface QuartzEventExecutor<T, E> {

    T create(Object bean, Method method);

    void triggerEvent(Object bean, Method method, E event);

}