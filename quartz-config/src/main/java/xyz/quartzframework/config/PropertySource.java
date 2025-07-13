package xyz.quartzframework.config;

import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;

public interface PropertySource extends ConfigurationSection {

    void reload();

    void save();

}