package dev.pk7r.spigot.starter.core.convert;

public class BooleanConvertService implements ConvertService<Boolean> {

    @Override
    public Boolean convert(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == Boolean.class || t == boolean.class;
    }
}
