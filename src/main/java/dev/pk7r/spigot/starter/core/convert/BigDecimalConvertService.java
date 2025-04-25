package dev.pk7r.spigot.starter.core.convert;

import java.math.BigDecimal;

public class BigDecimalConvertService implements ConvertService<BigDecimal> {

    @Override
    public BigDecimal convert(String value) {
        return BigDecimal.valueOf(Double.parseDouble(value));
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == BigDecimal.class;
    }
}