package dev.pk7r.spigot.starter.ioc.injector;

import dev.pk7r.spigot.starter.ioc.factory.bean.BeanFactory;

public interface BeanInjector {

    BeanFactory getBeanFactory();

    void inject(Class<?> clazz);

}