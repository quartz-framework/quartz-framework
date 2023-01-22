package dev.pk7r.spigot.starter.ioc.factory.event;

import dev.pk7r.spigot.starter.ioc.util.ReflectionUtil;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.pacesys.reflect.Reflect;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public interface EventFactory {

    Plugin getPlugin();

    void registerEvents(Listener listener);

}