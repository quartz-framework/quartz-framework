package xyz.quartzframework.core.application;

import lombok.val;
import org.bukkit.plugin.Plugin;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.context.AbstractPluginContext;
import xyz.quartzframework.core.context.SimplePluginContext;

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