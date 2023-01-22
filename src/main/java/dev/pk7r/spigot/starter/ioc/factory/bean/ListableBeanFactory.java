package dev.pk7r.spigot.starter.ioc.factory.bean;

import java.util.Collection;

public interface ListableBeanFactory {

    <T> Collection<T> getBeansOfType(Class<T> requiredType);

}