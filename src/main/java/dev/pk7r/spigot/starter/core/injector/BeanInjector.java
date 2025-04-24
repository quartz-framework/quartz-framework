package dev.pk7r.spigot.starter.core.injector;

import dev.pk7r.spigot.starter.core.factory.bean.BeanFactory;

public interface BeanInjector {

    BeanFactory getBeanFactory();

    void inject(Class<?> clazz);

}