package dev.pk7r.spigot.starter.core.convert;

public class FloatConvertService implements ConvertService<Float> {

    @Override
    public Float convert(String value) {
        return Float.parseFloat(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == Float.class || t == float.class;
    }
}
