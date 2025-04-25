package dev.pk7r.spigot.starter.core.convert;

import java.time.LocalDateTime;

public class LocalDateTimeConvertService implements ConvertService<LocalDateTime> {

    @Override
    public LocalDateTime convert(String value) {
        return LocalDateTime.parse(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == LocalDateTime.class;
    }
}