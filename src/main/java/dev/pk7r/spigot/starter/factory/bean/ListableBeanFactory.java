package dev.pk7r.spigot.starter.factory.bean;

import java.util.Collection;

public interface ListableBeanFactory {

    <T> Collection<T> getBeansOfType(Class<T> requiredType);

}