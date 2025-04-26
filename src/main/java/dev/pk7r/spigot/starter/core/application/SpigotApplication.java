package dev.pk7r.spigot.starter.core.application;

import dev.pk7r.spigot.starter.core.context.PluginContext;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SpigotApplication extends JavaPlugin implements ConfigurableApplication {

    private PluginContext context;

    @Override
    public final void onEnable() {
        main();
    }

    @Override
    public final void onDisable() {
        close();
    }

    @Override
    public PluginContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(PluginContext context) {
        this.context = context;
    }
}