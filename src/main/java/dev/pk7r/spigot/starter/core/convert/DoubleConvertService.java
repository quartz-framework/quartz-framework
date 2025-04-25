package dev.pk7r.spigot.starter.core.convert;

public class DoubleConvertService implements ConvertService<Double> {

    @Override
    public Double convert(String value) {
        return Double.parseDouble(value);
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == Double.class || t == double.class;
    }
}
