package xyz.quartzframework.config;

import java.util.Map;

public interface PropertyPostProcessor {

    <T> T process(String key, String source, Class<T> type);

    Map<String, String> getEnvironmentVariables();

}