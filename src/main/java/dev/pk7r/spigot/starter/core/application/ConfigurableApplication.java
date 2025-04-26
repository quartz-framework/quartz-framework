package dev.pk7r.spigot.starter.core.application;

import dev.pk7r.spigot.starter.core.context.PluginContext;
import dev.pk7r.spigot.starter.core.context.SimplePluginContext;
import lombok.val;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public interface ConfigurableApplication extends Plugin {

    PluginContext getContext();

    void main();

    void setContext(PluginContext context);

    default void run(Class<? extends ConfigurableApplication> applicationClass) {
        val context = SimplePluginContext.initialize(this, applicationClass);
        run(context);
    }

    default void run(PluginContext context) {
        setContext(context);
        context.start(this);
    }

    default void close() {
        val context = getContext();
        if (Objects.isNull(context)) return;
        context.close();
    }
}