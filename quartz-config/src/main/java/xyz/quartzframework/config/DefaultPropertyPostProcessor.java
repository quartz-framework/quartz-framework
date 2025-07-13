package xyz.quartzframework.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.convert.ConversionService;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
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
        log.trace("Processing property: '{}', extracted key='{}', fallback='{}'", match, key, fallback);
        val propertySource = propertySourceFactory.get(source);
        propertySource.reload();

        val rawValue = propertySource.get(key);
        val sourceValue = conversionService.convert(rawValue, String.class);

        log.trace("Resolved key='{}' from source='{}': raw='{}', string='{}'", key, source, rawValue, sourceValue);

        if (sourceValue != null) {
            val isEnv = ENV_VAR_PATTERN.matcher(sourceValue).matches();
            if (isEnv) {
                log.trace("Nested environment-like value detected: '{}', reprocessing...", sourceValue);
                return process(sourceValue, source, type);
            }
            T converted = conversionService.convert(sourceValue, type);
            log.trace("Converted value='{}' to type '{}': {}", sourceValue, type.getSimpleName(), converted);
            return converted;
        }
        val envValue = getEnvironmentVariables().get(key);
        if (envValue != null) {
            log.trace("Resolved key='{}' from environment variable: '{}'", key, envValue);
            return conversionService.convert(envValue, type);
        }
        val systemPropValue = System.getProperty(key);
        if (systemPropValue != null) {
            log.trace("Resolved key='{}' from system property: '{}'", key, systemPropValue);
            return conversionService.convert(systemPropValue, type);
        }

        if (fallback != null) {
            log.trace("Using fallback for key='{}': '{}'", key, fallback);
            return conversionService.convert(fallback, type);
        }
        throw new IllegalArgumentException("Could not find property: " + key);
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

}