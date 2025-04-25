package dev.pk7r.spigot.starter.core.convert;

public class StringConvertService implements ConvertService<String> {

    @Override
    public String convert(String value) {
        return String.valueOf(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == String.class;
    }
}
