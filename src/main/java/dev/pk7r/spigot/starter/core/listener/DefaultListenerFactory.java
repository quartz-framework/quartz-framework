package dev.pk7r.spigot.starter.core.listener;

import dev.pk7r.spigot.starter.core.annotation.Listen;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

@Getter
@NoProxy
@RequiredArgsConstructor
class DefaultListenerFactory implements ListenerFactory {

    private final Plugin plugin;

    private final PluginEventExecutor executor;

    public void registerEvents(Object bean) {
        getListenerMethods(bean).forEach(method -> registerEvent(bean, method));
    }

    @Override
    public void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    @SuppressWarnings("unchecked")
    private void registerEvent(Object bean, Method method) {
        val server = plugin.getServer();
        val eventType = (Class<? extends Event>) method.getParameters()[0].getType();
        val eventHandler = method.getAnnotation(EventHandler.class);
        val listen = method.getAnnotation(Listen.class);
        val priority = eventHandler != null
                ? eventHandler.priority()
                : (listen != null ? listen.priority() : EventPriority.NORMAL);
        val ignoreCancelled = eventHandler != null
                ? eventHandler.ignoreCancelled()
                : (listen != null && listen.ignoreCancelled());
        Listener listener = (bean instanceof Listener) ? (Listener) bean : new Listener() {}; // if not, create dummy
        server.getPluginManager().registerEvent(
                eventType,
                listener,
                priority,
                getExecutor().create(bean, method),
                plugin,
                ignoreCancelled
        );
    }

    private Stream<Method> getListenerMethods(Object bean) {
        val target = AopUtils.getTargetClass(bean);
        return Arrays.stream(ReflectionUtils.getAllDeclaredMethods(target))
                .filter(method ->
                        (method.isAnnotationPresent(EventHandler.class) || method.isAnnotationPresent(dev.pk7r.spigot.starter.core.annotation.Listen.class))
                                && method.getParameters().length == 1
                                && Event.class.isAssignableFrom(method.getParameters()[0].getType())
                );
    }
}
