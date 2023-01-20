package dev.pk7r.spigot.starter.ioc.factory;

import java.util.Collection;

public interface ListableBeanFactory {

    <T> Collection<T> getBeansOfType(Class<T> requiredType);

}