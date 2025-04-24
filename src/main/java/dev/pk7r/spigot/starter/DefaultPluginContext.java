package dev.pk7r.spigot.starter;

import dev.pk7r.spigot.starter.annotation.PluginApplication;
import dev.pk7r.spigot.starter.context.PluginContext;
import dev.pk7r.spigot.starter.exception.ContextInitializationException;
import dev.pk7r.spigot.starter.factory.bean.BeanFactory;
import dev.pk7r.spigot.starter.factory.event.EventFactory;
import dev.pk7r.spigot.starter.injector.BeanInjector;
import dev.pk7r.spigot.starter.lifecycle.LifecycleMethodsInspector;
import dev.pk7r.spigot.starter.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.util.BeanUtil;
import dev.pk7r.spigot.starter.util.ClassUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Getter
@Setter(AccessLevel.PRIVATE)
public class DefaultPluginContext implements PluginContext {

    private final UUID id = UUID.randomUUID();

    private final long initializationTime = System.currentTimeMillis();

    private final long startupTime;

    private Plugin plugin;

    private String pluginName;

    private PluginApplication pluginApplication;

    private BeanFactory beanFactory;

    private EventFactory eventFactory;

    private BeanInjector beanInjector;

    private BeanDefinitionRegistry beanDefinitionRegistry;

    private LifecycleMethodsInspector lifecycleMethodsInspector;

    public DefaultPluginContext(SpigotApplication application) {
        this(application, (p) -> {});
    }

    public DefaultPluginContext(SpigotApplication application, Consumer<PluginContext> afterStart) {
        synchronized (DefaultPluginContext.class) {
            if (!application.getClass().isAnnotationPresent(PluginApplication.class)) {
                throw new ContextInitializationException("Main class must be annotated with @PluginMain");
            }
            val pluginMain = application.getClass().getAnnotation(PluginApplication.class);
            setPlugin(application);
            setPluginApplication(pluginMain);
            setPluginName(application.getName());
            setEventFactory(new DefaultEventFactory(getPlugin()));
            setBeanDefinitionRegistry(new DefaultBeanDefinitionRegistry(this));
            setBeanFactory(new DefaultBeanFactory(getBeanDefinitionRegistry()));
            setBeanInjector(new DefaultBeanInjector(getBeanFactory()));
            setLifecycleMethodsInspector(new DefaultLifecycleMethodsInspector(getBeanFactory()));
            registerDefaultBeans();
            startContext();
            this.startupTime = System.currentTimeMillis();
            afterStart.accept(this);
            log.info("Context {} initialized after {} ms", getId(), getStartupTime() - getInitializationTime());
        }
    }

    @Override
    public void startContext() {
        val injectables = DefaultClassScanner.getInstance().scan(
                getPluginApplication().basePackages(),
                getPluginApplication().exclude(),
                (BeanUtil::isInjectable),
                getPluginApplication().verbose());
        injectables
                .stream()
                .filter(clazz -> !clazz.isAnnotation())
                .filter(injectable -> {
                    if (BeanUtil.isConditionalOnClass(injectable)) {
                        val classNames = BeanUtil.getConditionalOnClass(injectable);
                        return classNames.stream().anyMatch(ClassUtil::isClassLoaded);
                    } else return true;
                })
                .forEach(injectable -> getBeanDefinitionRegistry().registerBeanDefinition(injectable));
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .peek(beanDefinition -> getLifecycleMethodsInspector().registerLifecycleMethods(beanDefinition.getLiteralType()))
                .peek(beanDefinition -> getBeanInjector().inject(beanDefinition.getLiteralType()))
                .forEach(beanDefinition -> {
                    val postConstructMethods = getLifecycleMethodsInspector().getPostConstructMethods();
                    postConstructMethods
                            .stream()
                            .filter(m -> m.getDeclaringClass().equals(beanDefinition.getLiteralType()))
                            .findFirst()
                            .ifPresent(postConstructMethod -> getLifecycleMethodsInspector().invokeLifecycleMethod(postConstructMethod));
                });
        getBeanFactory().getBeansOfType(Listener.class).forEach(listener -> getEventFactory().registerEvents(listener));
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
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "Plugin", Plugin.class, getPlugin());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "Context", PluginContext.class, this);
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "BeanFactory", BeanFactory.class, getBeanFactory());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "BeanDefinitionRegistry", BeanDefinitionRegistry.class, getBeanDefinitionRegistry());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "BeanInjector", BeanInjector.class, getBeanInjector());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "LifecycleMethodsInspector", LifecycleMethodsInspector.class, getLifecycleMethodsInspector());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "EventFactory", EventFactory.class, getEventFactory());
    }
}