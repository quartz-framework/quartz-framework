package xyz.quartzframework.core.bean.strategy;

import xyz.quartzframework.core.annotation.NoProxy;

import java.lang.reflect.Method;

@NoProxy
public class DefaultBeanNameStrategy implements BeanNameStrategy {

    @Override
    public String generateBeanName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    @Override
    public String generateBeanName(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }
}