package dev.pk7r.spigot.starter.ioc.context;

import dev.pk7r.spigot.starter.ioc.annotation.PluginMain;
import dev.pk7r.spigot.starter.ioc.factory.bean.BeanFactory;
import org.bukkit.plugin.Plugin;

public interface PluginContext extends BeanFactory {

    void startContext();

    void close();

    Plugin getPlugin();

    String getPluginName();

    PluginMain getPluginMain();

}