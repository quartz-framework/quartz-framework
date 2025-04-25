package dev.pk7r.spigot.starter.core.property;

import org.bukkit.configuration.ConfigurationSection;

public interface PropertySource extends ConfigurationSection {

    void reload();

    void save();

}