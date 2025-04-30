package xyz.quartzframework.core.bean.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;

import java.net.URLClassLoader;

@NoProxy
public interface PluginBeanFactory extends BeanFactory, ListableBeanFactory {

    URLClassLoader getClassLoader();

    PluginBeanDefinitionRegistry getRegistry();

}