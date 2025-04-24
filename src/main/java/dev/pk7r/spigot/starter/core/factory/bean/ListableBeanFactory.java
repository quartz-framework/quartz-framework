package dev.pk7r.spigot.starter.core.factory.bean;

import java.util.Collection;

public interface ListableBeanFactory {

    <T> Collection<T> getBeansOfType(Class<T> requiredType);

}