package dev.pk7r.spigot.starter.core.bean.factory;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.bean.registry.PluginBeanDefinitionRegistry;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;

import java.net.URLClassLoader;

@NoProxy
public interface PluginBeanFactory extends BeanFactory, ListableBeanFactory {

    URLClassLoader getClassLoader();

    PluginBeanDefinitionRegistry getRegistry();

}