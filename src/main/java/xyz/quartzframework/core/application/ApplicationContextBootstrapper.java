package xyz.quartzframework.core.application;

import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import xyz.quartzframework.core.annotation.*;
import xyz.quartzframework.core.context.PluginContext;

import java.net.URLClassLoader;

@NoProxy
@ContextBootstrapper
@RequiredArgsConstructor
public class ApplicationContextBootstrapper {

    private final PluginContext context;

    @Provide
    @Preferred
    PluginApplication application() {
        return context.getPluginApplication();
    }

    @Provide
    @Preferred
    Plugin plugin() {
        return context.getPlugin();
    }

    @Provide
    @Preferred
    Server server(Plugin plugin) {
        return plugin.getServer();
    }

    @Provide
    @Preferred
    BukkitScheduler bukkitScheduler(Server server) {
        return server.getScheduler();
    }

    @Provide
    @Preferred
    PluginManager pluginManager(Server server) {
        return server.getPluginManager();
    }

    @Provide
    @Preferred
    PluginLoader pluginLoader(Plugin plugin) {
        return plugin.getPluginLoader();
    }

    @Provide
    @Preferred
    ResourceLoader resourceLoader(URLClassLoader loader) {
        return new DefaultResourceLoader(loader);
    }
}