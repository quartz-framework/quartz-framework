package xyz.quartzframework.core.property;

import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;

public interface PropertySource extends ConfigurationSection {

    void reload();

    void save();

}