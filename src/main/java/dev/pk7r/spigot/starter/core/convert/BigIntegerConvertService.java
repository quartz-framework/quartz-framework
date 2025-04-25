package dev.pk7r.spigot.starter.core.convert;

import java.math.BigInteger;

public class BigIntegerConvertService implements ConvertService<BigInteger> {

    @Override
    public BigInteger convert(String value) {
        return BigInteger.valueOf(Long.parseLong(value));
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == BigInteger.class;
    }
}