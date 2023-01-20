package dev.pk7r.spigot.starter.ioc.context;

import dev.pk7r.spigot.starter.ioc.annotation.PluginMain;
import dev.pk7r.spigot.starter.ioc.exception.ContextInitializationException;
import dev.pk7r.spigot.starter.ioc.factory.BeanFactory;
import dev.pk7r.spigot.starter.ioc.factory.DefaultBeanFactory;
import dev.pk7r.spigot.starter.ioc.injector.BeanInjector;
import dev.pk7r.spigot.starter.ioc.injector.DefaultBeanInjector;
import dev.pk7r.spigot.starter.ioc.lifecycle.DefaultLifecycleMethodsInspector;
import dev.pk7r.spigot.starter.ioc.lifecycle.LifecycleMethodsInspector;
import dev.pk7r.spigot.starter.ioc.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.ioc.registry.DefaultBeanDefinitionRegistry;
import dev.pk7r.spigot.starter.ioc.scanner.DefaultClassScanner;
import dev.pk7r.spigot.starter.ioc.util.BeanUtil;
import dev.pk7r.spigot.starter.ioc.util.ClassUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

@Getter
@Setter(AccessLevel.PRIVATE)
public class DefaultPluginContext implements PluginContext {

    private Plugin plugin;

    private String pluginName;

    private PluginMain pluginMain;

    private BeanFactory beanFactory;

    private BeanInjector beanInjector;

    private BeanDefinitionRegistry beanDefinitionRegistry;

    private LifecycleMethodsInspector lifecycleMethodsInspector;

    public DefaultPluginContext(Plugin plugin) {
        synchronized (DefaultPluginContext.class) {
            if (!plugin.getClass().isAnnotationPresent(PluginMain.class)) {
                throw new ContextInitializationException("Main class must be annotated with @PluginMain");
            }
            val pluginMain = plugin.getClass().getAnnotation(PluginMain.class);
            setPlugin(plugin);
            setPluginMain(pluginMain);
            setPluginName(plugin.getName());
            setBeanDefinitionRegistry(new DefaultBeanDefinitionRegistry(this));
            setBeanFactory(new DefaultBeanFactory(getBeanDefinitionRegistry()));
            setBeanInjector(new DefaultBeanInjector(getBeanFactory()));
            setLifecycleMethodsInspector(new DefaultLifecycleMethodsInspector(getBeanFactory()));
            registerDefaultBeans();
            startContext();
        }
    }

    @Override
    public void startContext() {
        val injectables = DefaultClassScanner.getInstance().scan(
                getPluginMain().basePackages(),
                getPluginMain().exclude(),
                (clazz -> {
                    if (BeanUtil.isInjectable(clazz)) {
                        return true;
                    } else return BeanUtil.isBean(clazz);
                }),
                getPluginMain().verbose());
        injectables
                .stream()
                .filter(injectable -> {
                    if (BeanUtil.isConditionalOnClass(injectable)) {
                        val classNames = BeanUtil.getConditionalOnClass(injectable);
                        return classNames.stream().anyMatch(ClassUtil::isClassLoaded);
                    } else return true;
                })
                .peek(injectable -> getLifecycleMethodsInspector().registerLifecycleMethods(injectable))
                .peek(injectable -> {
                    if (BeanUtil.isPrimary(injectable)) {
                        getBeanDefinitionRegistry().registerBeanDefinition(injectable);
                    }
                })
                .forEach(injectable -> {
                    if (!BeanUtil.isPrimary(injectable)) {
                        getBeanDefinitionRegistry().registerBeanDefinition(injectable);
                    }
                });
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .forEach(beanDefinition -> getBeanInjector().inject(beanDefinition.getLiteralType()));
        // TODO: Register all events here
        getLifecycleMethodsInspector().getPostConstructMethods().forEach(getLifecycleMethodsInspector()::invokeLifecycleMethod);
    }

    @Override
    public void close() {
        getLifecycleMethodsInspector().getPreDestroyMethods().forEach(getLifecycleMethodsInspector()::invokeLifecycleMethod);
        getLifecycleMethodsInspector().destroyMethods();
        getBeanDefinitionRegistry().getBeanDefinitions().clear();
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return getBeanFactory().getBean(requiredType);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        return getBeanFactory().getBean(beanName, requiredType);
    }

    @Override
    public boolean containsBean(String beanName, Class<?> requiredType) {
        return getBeanFactory().containsBean(beanName, requiredType);
    }

    @Override
    public <T> Collection<T> getBeansOfType(Class<T> requiredType) {
        return getBeanFactory().getBeansOfType(requiredType);
    }

    private void registerDefaultBeans() {
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName(), Plugin.class, getPlugin());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "Context", PluginContext.class, this);
    }

    // TODO: @ConditionalOnBean beans
    // TODO: @Lazy & @Eager beans
    // TODO: Constructor injection

}