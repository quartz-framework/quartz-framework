package xyz.quartzframework.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import xyz.quartzframework.Quartz;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultPropertySourceFactory implements PropertySourceFactory {

    private final Map<String, PropertySource> sources = new HashMap<>();

    private final Quartz<?> quartz;

    @Override
    public PropertySource get(String name) {
        return sources.computeIfAbsent(name, this::loadConfiguration);
    }

    @SneakyThrows
    private PropertySource loadConfiguration(String name) {
        val fileName = String.format("%s.yml", name);
        val configFile = new File(quartz.getDataFolder(), fileName);
        if (!configFile.exists()) {
            val internal = quartz.getClass().getClassLoader().getResource(fileName);
            if (internal != null) {
                quartz.saveResource(fileName, false);
            } else {
                configFile.getParentFile().mkdirs();
                if (configFile.createNewFile()) {
                    log.info("Created fallback {} configuration file", fileName);
                }
            }
        }
        val yamlConfiguration = YamlConfiguration.loadConfiguration(configFile);
        return new PropertySourceFile(yamlConfiguration, configFile);
    }
}