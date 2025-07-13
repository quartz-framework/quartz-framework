package xyz.quartzframework.beans.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.definition.QuartzBeanDefinitionRegistry;

import java.net.URLClassLoader;

@NoProxy
public interface QuartzBeanFactory extends BeanFactory, ListableBeanFactory {

    URLClassLoader getClassLoader();

    QuartzBeanDefinitionRegistry getRegistry();

}