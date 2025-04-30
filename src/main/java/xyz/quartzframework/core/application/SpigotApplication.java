package xyz.quartzframework.core.application;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.condition.annotation.ActivateWhenClassPresent;
import xyz.quartzframework.core.context.AbstractPluginContext;

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