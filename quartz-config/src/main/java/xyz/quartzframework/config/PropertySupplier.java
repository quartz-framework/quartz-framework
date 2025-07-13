package xyz.quartzframework.config;

import java.util.function.Supplier;

public class PropertySupplier<T> implements Supplier<T> {

    private final Class<T> clazz;

    private final String source;

    private final String expression;

    private final PropertyPostProcessor propertyPostProcessor;

    public PropertySupplier(PropertyPostProcessor propertyPostProcessor, String source, String expression, Class<T> clazz) {
        this.propertyPostProcessor = propertyPostProcessor;
        this.source = source;
        this.expression = expression;
        this.clazz = clazz;
    }

    @Override
    public T get() {
        return propertyPostProcessor.process(source, expression, clazz);
    }
}