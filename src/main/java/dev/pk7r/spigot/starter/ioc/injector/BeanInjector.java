package dev.pk7r.spigot.starter.ioc.injector;

import dev.pk7r.spigot.starter.ioc.factory.BeanFactory;

public interface BeanInjector {

    BeanFactory getBeanFactory();

    void inject(Class<?> clazz);

}