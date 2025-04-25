package dev.pk7r.spigot.starter.core.convert;

import java.time.Instant;

public class InstantConvertService implements ConvertService<Instant> {

    @Override
    public Instant convert(String value) {
        return Instant.parse(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == Instant.class;
    }
}