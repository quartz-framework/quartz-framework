package dev.pk7r.spigot.starter.core;

import dev.pk7r.spigot.starter.core.context.SimplePluginContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PUBLIC)
public abstract class SpigotApplication extends JavaPlugin {

    private SpigotApplication application;

    private SimplePluginContext context;

    @Override
    public void onLoad() {
        setApplication(this);
    }

    @Override
    public void onEnable() {
        setContext(new SimplePluginContext(getApplication()));
    }

    @Override
    public void onDisable() {
        val context = getContext();
        if (Objects.isNull(context)) return;
        context.close();
    }
}