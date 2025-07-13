package xyz.quartzframework.context;

import xyz.quartzframework.Quartz;
import xyz.quartzframework.QuartzPlugin;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.definition.QuartzBeanDefinitionBuilder;
import xyz.quartzframework.beans.definition.QuartzBeanDefinitionRegistry;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.beans.strategy.BeanNameStrategy;

import java.util.UUID;

@NoProxy
public interface QuartzContext<T> {

    UUID getId();

    void start(Quartz<T> quartz);

    void close();

    default boolean isVerbose() {
        return getInformationMetadata().verbose();
    }

    Quartz<T> getQuartz();

    QuartzPlugin getInformationMetadata();

    QuartzBeanFactory getBeanFactory();

    BeanNameStrategy getBeanNameStrategy();

    QuartzBeanDefinitionRegistry getBeanDefinitionRegistry();

    QuartzBeanDefinitionBuilder getBeanDefinitionBuilder();

    void registerSingleton(Object instance);

    void registerSingleton(Class<?> clazz, Object instance);

    void registerSingleton(String beanName, Class<?> clazz, Object instance);

    void registerSingleton(String beanName, TypeMetadata metadata, Object instance);

}