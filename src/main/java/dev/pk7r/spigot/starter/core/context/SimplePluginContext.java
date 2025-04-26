package dev.pk7r.spigot.starter.core.context;

import dev.pk7r.spigot.starter.core.application.ConfigurableApplication;

public class SimplePluginContext extends AbstractPluginContext {

    private SimplePluginContext(ConfigurableApplication application, Class<? extends ConfigurableApplication> applicationClass) {
        super(application, applicationClass);
    }

    public static SimplePluginContext initialize(ConfigurableApplication application, Class<? extends ConfigurableApplication> applicationClass) {
        return new SimplePluginContext(application, applicationClass);
    }
}