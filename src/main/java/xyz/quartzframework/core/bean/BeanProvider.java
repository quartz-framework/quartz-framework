package xyz.quartzframework.core.bean;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;

import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class BeanProvider<T> implements ObjectProvider<T> {

    private final PluginBeanFactory factory;

    private final Class<T> type;

    @Override
    public Stream<T> stream() {
        return factory.getBeansOfType(type).values().stream();
    }

    public Optional<T> optional() {
        return this.stream().findFirst();
    }

    public static <T> BeanProvider<T> of(PluginBeanFactory factory, Class<T> type) {
        return new BeanProvider<>(factory, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanProvider<T> of(PluginBeanFactory factory, ResolvableType type) {
        return (BeanProvider<T>) BeanProvider.of(factory, type.getRawClass());
    }
}