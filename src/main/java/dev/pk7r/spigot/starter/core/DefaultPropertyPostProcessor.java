package dev.pk7r.spigot.starter.core;

import dev.pk7r.spigot.starter.core.convert.ConvertService;
import dev.pk7r.spigot.starter.core.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.factory.PropertySourceFactory;
import dev.pk7r.spigot.starter.core.property.PropertyPostProcessor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
class DefaultPropertyPostProcessor implements PropertyPostProcessor {

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");
    @Getter
    private final PropertySourceFactory propertySourceFactory;

    private final BeanFactory beanFactory;

    @Override
    public <T> T process(String match, String source, Class<T> type) {
        val matcher = ENV_VAR_PATTERN.matcher(match);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Property not matches with pattern '${([^:}]+)(?::([^}]*))?}': " + match);
        }
        val key = matcher.group(1);
        val fallback = matcher.group(2);
        val propertySource = propertySourceFactory.get(source);
        val sourceValue = propertySource.getString(key);
        val convertService = getConvertService(type);
        if (sourceValue != null) {
            val isEnv = ENV_VAR_PATTERN.matcher(sourceValue).matches();
            if (isEnv) {
                return process(sourceValue, source, type);
            }
            return convertService.convert(sourceValue);
        }
        val environmentVariableValue = getEnvironmentVariables().get(key);
        if (environmentVariableValue != null) {
            return convertService.convert(environmentVariableValue);
        }
        if (fallback != null) {
            return convertService.convert(fallback);
        }
        throw new IllegalArgumentException("Could not find property: " + key);
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

    @SuppressWarnings("unchecked")
    private <T> ConvertService<T> getConvertService(Class<T> type) {
        return (ConvertService<T>) beanFactory.getBeansOfType(ConvertService.class)
                .stream()
                .filter(service -> service.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No ConvertService found for type: " + type.getName()));
    }
}