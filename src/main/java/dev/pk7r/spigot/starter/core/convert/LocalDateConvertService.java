package dev.pk7r.spigot.starter.core.convert;

import java.time.LocalDate;

public class LocalDateConvertService implements ConvertService<LocalDate> {

    @Override
    public LocalDate convert(String value) {
        return LocalDate.parse(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == LocalDate.class;
    }
}