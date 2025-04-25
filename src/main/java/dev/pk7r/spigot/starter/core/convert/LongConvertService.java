package dev.pk7r.spigot.starter.core.convert;

public class LongConvertService implements ConvertService<Long> {

    @Override
    public Long convert(String value) {
        return Long.parseLong(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == Long.class;
    }
}
