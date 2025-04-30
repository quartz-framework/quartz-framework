package xyz.quartzframework.core.property;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DefaultPropertySourceFactory implements PropertySourceFactory {

    private final Map<String, PropertySource> sources = new HashMap<>();

    private final Plugin plugin;

    @Override
    public PropertySource get(String name) {
        return sources.computeIfAbsent(name, this::loadConfiguration);
    }

    @SneakyThrows
    private PropertySource loadConfiguration(String name) {
        val configFile = new File(plugin.getDataFolder(), String.format("%s.yml", name));
        if (!configFile.exists()) {
            plugin.saveResource(String.format("%s.yml", name), false);
        }
        val yamlConfiguration = YamlConfiguration.loadConfiguration(configFile);
        return new PropertySourceFile(yamlConfiguration, configFile);
    }
}