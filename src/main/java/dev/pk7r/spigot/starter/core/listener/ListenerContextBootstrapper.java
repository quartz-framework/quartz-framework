package dev.pk7r.spigot.starter.core.listener;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Preferred;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenBeanMissing;
import dev.pk7r.spigot.starter.core.session.PlayerSession;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

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