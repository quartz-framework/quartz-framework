package dev.pk7r.spigot.starter.core;

import dev.pk7r.spigot.starter.core.context.PluginContext;
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

    private PluginContext context;

    @Override
    public final void onLoad() {
        setApplication(this);
    }

    @Override
    public final void onEnable() {
        setContext(new DefaultPluginContext(getApplication()));
        onStart();
    }

    @Override
    public final void onDisable() {
        val context = getContext();
        if (Objects.isNull(context)) return;
        context.close();
        onClose();
    }

    public abstract void onStart();

    public abstract void onClose();

}