package xyz.quartzframework.core.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Preferred;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;
import xyz.quartzframework.core.session.PlayerSession;

@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
public class ListenerContextBootstrapper {

    @Provide
    @Preferred
    PluginEventExecutor pluginEventExecutor(PlayerSession session) {
        return new PluginEventExecutor(session);
    }

    @Provide
    @ActivateWhenBeanMissing(ListenerFactory.class)
    ListenerFactory listenerFactory(Plugin plugin, PluginEventExecutor executor) {
        return new DefaultListenerFactory(plugin, executor);
    }
}