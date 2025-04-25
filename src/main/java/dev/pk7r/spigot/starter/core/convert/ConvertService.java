package dev.pk7r.spigot.starter.core.convert;

public interface ConvertService<T> {

    T convert(String string);

    boolean supports(Class<?> t);

}