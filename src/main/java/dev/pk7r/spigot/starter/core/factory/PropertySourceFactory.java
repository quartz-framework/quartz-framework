package dev.pk7r.spigot.starter.core.factory;

import dev.pk7r.spigot.starter.core.property.PropertySource;

public interface PropertySourceFactory {

    PropertySource get(String name);

}