package dev.pk7r.spigot.starter.core.application;

import co.aikar.commands.lib.timings.TimingManager;
import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.PluginApplication;
import dev.pk7r.spigot.starter.core.annotation.Primary;
import dev.pk7r.spigot.starter.core.context.PluginContext;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

@RequiredArgsConstructor
@AutoConfiguration(force = true)
public class ApplicationAutoConfiguration {

    private final PluginContext context;

    @Bean
    @Primary
    PluginApplication application() {
        return context.getPluginApplication();
    }

    @Bean
    @Primary
    Plugin plugin() {
        return context.getPlugin();
    }

    @Bean
    @Primary
    Server server(Plugin plugin) {
        return plugin.getServer();
    }

    @Bean
    @Primary
    BukkitScheduler bukkitScheduler(Server server) {
        return server.getScheduler();
    }

    @Bean
    @Primary
    PluginManager pluginManager(Server server) {
        return server.getPluginManager();
    }

    @Bean
    @Primary
    PluginLoader pluginLoader(Plugin plugin) {
        return plugin.getPluginLoader();
    }

    @Bean
    @Primary
    TimingManager timingManager(Plugin plugin) {
        return TimingManager.of(plugin);
    }
}