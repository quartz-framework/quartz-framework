package dev.pk7r.spigot.starter.core.application;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenClassPresent;
import dev.pk7r.spigot.starter.core.context.AbstractPluginContext;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URLClassLoader;

@NoProxy
@ActivateWhenClassPresent(JavaPlugin.class)
public abstract class SpigotApplication extends JavaPlugin implements ConfigurableApplication {

    private AbstractPluginContext context;

    @Override
    public final void onEnable() {
        main();
    }

    @Override
    public final void onDisable() {
        close();
    }

    @Override
    public AbstractPluginContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(AbstractPluginContext context) {
        this.context = context;
    }

    public void run(Class<? extends SpigotApplication> applicationClass) {
        run(applicationClass, (URLClassLoader) getClassLoader());
    }
}