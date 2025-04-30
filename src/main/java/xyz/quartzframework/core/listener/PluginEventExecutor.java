package xyz.quartzframework.core.listener;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.plugin.EventExecutor;
import org.springframework.aop.support.AopUtils;
import xyz.quartzframework.core.session.PlayerSession;
import xyz.quartzframework.core.util.EventUtil;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class PluginEventExecutor {

    private final PlayerSession session;

    public EventExecutor create(Object bean, Method method) {
        val eventType = method.getParameters()[0].getType();
        return (listener, event) -> {
            if (!eventType.isInstance(event)) return;
            session.runWithSender(EventUtil.getSender(event),
                    () -> triggerEvent(bean, method, event));
        };
    }

    @SneakyThrows
    private void triggerEvent(Object bean, Method method, Event event) {
        AopUtils.invokeJoinpointUsingReflection(bean, method, new Object[]{event});
    }
}