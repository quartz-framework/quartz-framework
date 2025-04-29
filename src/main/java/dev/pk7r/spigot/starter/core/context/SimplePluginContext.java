package dev.pk7r.spigot.starter.core.context;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.application.ConfigurableApplication;

import java.net.URLClassLoader;

@NoProxy
public class SimplePluginContext extends AbstractPluginContext {

    private SimplePluginContext(ConfigurableApplication application, Class<? extends ConfigurableApplication> applicationClass, URLClassLoader classLoader) {
        super(application, applicationClass, classLoader);
    }

    public static SimplePluginContext initialize(ConfigurableApplication application, Class<? extends ConfigurableApplication> applicationClass, URLClassLoader classLoader) {
        return new SimplePluginContext(application, applicationClass, classLoader);
    }
}