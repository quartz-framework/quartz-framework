package dev.pk7r.spigot.starter.core.application;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.context.AbstractPluginContext;
import dev.pk7r.spigot.starter.core.context.SimplePluginContext;
import lombok.val;
import org.bukkit.plugin.Plugin;

import java.net.URLClassLoader;
import java.util.Objects;

@NoProxy
public interface ConfigurableApplication extends Plugin {

    AbstractPluginContext getContext();

    void main();

    void setContext(AbstractPluginContext context);

    default void run(Class<? extends ConfigurableApplication> applicationClass, URLClassLoader classLoader) {
        val context = SimplePluginContext.initialize(this, applicationClass, classLoader);
        run(context);
    }

    default void run(AbstractPluginContext context) {
        setContext(context);
        context.start(this);
    }

    default void close() {
        val context = getContext();
        if (Objects.isNull(context)) return;
        context.close();
    }
}