package dev.pk7r.spigot.starter.core.property;

import dev.pk7r.spigot.starter.core.factory.PropertySourceFactory;

import java.util.Map;

public interface PropertyPostProcessor {

    <T> T process(String key, String source, Class<T> type);

    Map<String, String> getEnvironmentVariables();

    PropertySourceFactory getPropertySourceFactory();

}