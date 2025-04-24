package dev.pk7r.spigot.starter.core.context;

import dev.pk7r.spigot.starter.core.annotation.PluginApplication;
import dev.pk7r.spigot.starter.core.factory.bean.BeanFactory;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface PluginContext extends BeanFactory {

    UUID getId();

    long getInitializationTime();

    long getStartupTime();

    void startContext();

    void close();

    Plugin getPlugin();

    String getPluginName();

    PluginApplication getPluginApplication();

}