package dev.pk7r.spigot.starter.injector;

import dev.pk7r.spigot.starter.factory.bean.BeanFactory;

public interface BeanInjector {

    BeanFactory getBeanFactory();

    void inject(Class<?> clazz);

}