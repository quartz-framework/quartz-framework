package dev.pk7r.spigot.starter.core.listener;

import dev.pk7r.spigot.starter.core.util.ReflectionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.pacesys.reflect.Reflect;

import java.lang.reflect.Method;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
class DefaultListenerFactory implements ListenerFactory {

    private final Plugin plugin;

    @Override
    public void registerEvents(Listener listener) {
        getListenerMethods(listener).forEach(method -> registerEvents(listener, method));
    }

    @Override
    public void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    @SuppressWarnings("unchecked")
    private void registerEvents(Listener listener, Method method) {
        val handler = method.getAnnotation(EventHandler.class);
        val eventType = (Class<? extends Event>) method.getParameters()[0].getType();
        getPlugin().getServer().getPluginManager().registerEvent(
                eventType,
                listener,
                handler == null ? EventPriority.NORMAL : handler.priority(),
                create(method),
                getPlugin(),
                handler == null || handler.ignoreCancelled());
    }

    private Stream<Method> getListenerMethods(Listener listener) {
        val target = listener.getClass();
        return ReflectionUtil.getMethods(Reflect.MethodType.INSTANCE, target, EventHandler.class)
                .stream()
                .filter(method -> method.getParameters().length == 1)
                .filter(method -> Event.class.isAssignableFrom(method.getParameters()[0].getType()));
    }

    public EventExecutor create(Method method) {
        val eventType = method.getParameters()[0].getType();
        return (listener, event) -> {
            if (!eventType.isInstance(event)) return;
            triggerEvent(method, listener, event);
        };
    }

    @SneakyThrows
    private void triggerEvent(Method method, Listener listener, Event event) {
        method.setAccessible(true);
        method.invoke(listener, event);
        method.setAccessible(false);
    }
}
