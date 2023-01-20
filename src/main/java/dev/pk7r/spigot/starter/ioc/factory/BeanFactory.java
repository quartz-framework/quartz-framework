package dev.pk7r.spigot.starter.ioc.factory;

public interface BeanFactory extends ListableBeanFactory {

    <T> T getBean(Class<T> requiredType);

    <T> T getBean(String beanName, Class<T> requiredType);

    boolean containsBean(String beanName, Class<?> requiredType);

}