package dev.pk7r.spigot.starter.core.convert;

import java.time.LocalTime;

public class LocalTimeConvertService implements ConvertService<LocalTime> {

    @Override
    public LocalTime convert(String value) {
        return LocalTime.parse(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == LocalTime.class;
    }
}