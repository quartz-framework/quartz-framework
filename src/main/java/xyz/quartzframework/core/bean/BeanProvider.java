package xyz.quartzframework.core.bean;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;

import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class BeanProvider<T> implements ObjectProvider<T> {

    private final PluginBeanFactory factory;

    private final Class<T> type;

    @NonNull
    @Override
    public Stream<T> stream() {
        return factory.getBeansOfType(type).values().stream();
    }

    public Optional<T> first() {
        return this.stream().findFirst();
    }

    public Optional<T> any() {
        return this.stream().findAny();
    }

    public T find(String beanName) {
        return this.factory.getBean(beanName, type);
    }

    public static <T> BeanProvider<T> of(PluginBeanFactory factory, Class<T> type) {
        return new BeanProvider<>(factory, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanProvider<T> of(PluginBeanFactory factory, ResolvableType type) {
        return (BeanProvider<T>) BeanProvider.of(factory, type.getRawClass());
    }
}