package dev.pk7r.spigot.starter.core.convert;

public class IntegerConvertService implements ConvertService<Integer> {

    @Override
    public Integer convert(String value) {
        return Integer.parseInt(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == Integer.class || t == int.class;
    }
}
