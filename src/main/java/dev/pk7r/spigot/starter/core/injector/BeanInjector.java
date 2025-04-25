package dev.pk7r.spigot.starter.core.injector;

import dev.pk7r.spigot.starter.core.factory.BeanFactory;

public interface BeanInjector {

    BeanFactory getBeanFactory();

    void inject(Class<?> clazz);

}