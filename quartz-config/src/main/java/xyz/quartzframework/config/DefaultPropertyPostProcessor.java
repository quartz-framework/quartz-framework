package xyz.quartzframework.config;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.convert.ConversionService;

import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class DefaultPropertyPostProcessor implements PropertyPostProcessor {

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");

    private final PropertySourceFactory propertySourceFactory;

    private final ConversionService conversionService;

    @Override
    public <T> T process(String match, String source, Class<T> type) {
        val matcher = ENV_VAR_PATTERN.matcher(match);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Property not matches with pattern '${([^:}]+)(?::([^}]*))?}': " + match);
        }
        val key = matcher.group(1);
        val fallback = matcher.group(2);
        val propertySource = propertySourceFactory.get(source);
        propertySource.reload();
        val sourceValue = propertySource.getString(key);
        if (sourceValue != null) {
            val isEnv = ENV_VAR_PATTERN.matcher(sourceValue).matches();
            if (isEnv) {
                return process(sourceValue, source, type);
            }
            return conversionService.convert(sourceValue, type);
        }
        val environmentVariableValue = getEnvironmentVariables().get(key);
        if (environmentVariableValue != null) {
            return conversionService.convert(environmentVariableValue, type);
        }
        val systemPropertyValue = System.getProperty(key);
        if (systemPropertyValue != null) {
            return conversionService.convert(systemPropertyValue, type);
        }
        if (fallback != null) {
            return conversionService.convert(fallback, type);
        }
        throw new IllegalArgumentException("Could not find property: " + key);
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

}