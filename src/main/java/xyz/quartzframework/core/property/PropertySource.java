package xyz.quartzframework.core.property;

import org.bukkit.configuration.ConfigurationSection;

public interface PropertySource extends ConfigurationSection {

    void reload();

    void save();

}