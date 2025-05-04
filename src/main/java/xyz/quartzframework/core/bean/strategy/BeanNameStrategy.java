package xyz.quartzframework.core.bean.strategy;

import xyz.quartzframework.core.bean.annotation.NoProxy;

import java.lang.reflect.Method;

@NoProxy
public interface BeanNameStrategy {

    String generateBeanName(Class<?> clazz);

    String generateBeanName(Method method);
}