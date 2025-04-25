package dev.pk7r.spigot.starter.core.factory;

import java.util.Collection;

public interface ListableBeanFactory {

    <T> Collection<T> getBeansOfType(Class<T> requiredType);

}