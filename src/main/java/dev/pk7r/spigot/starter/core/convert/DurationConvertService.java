package dev.pk7r.spigot.starter.core.convert;

import java.time.Duration;

public class DurationConvertService implements ConvertService<Duration> {

    @Override
    public Duration convert(String value) {
        return Duration.parse(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == Duration.class;
    }
}