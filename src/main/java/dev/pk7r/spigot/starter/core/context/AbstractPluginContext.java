package dev.pk7r.spigot.starter.core.context;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.PluginApplication;
import dev.pk7r.spigot.starter.core.application.ConfigurableApplication;
import dev.pk7r.spigot.starter.core.bean.BeanDefinition;
import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.bean.factory.DefaultBeanFactory;
import dev.pk7r.spigot.starter.core.bean.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.bean.registry.DefaultBeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.bean.strategy.BeanNameStrategy;
import dev.pk7r.spigot.starter.core.bean.strategy.DefaultBeanNameStrategy;
import dev.pk7r.spigot.starter.core.event.ContextCloseEvent;
import dev.pk7r.spigot.starter.core.event.ContextLoadedEvent;
import dev.pk7r.spigot.starter.core.event.ContextStartedEvent;
import dev.pk7r.spigot.starter.core.exception.ContextInitializationException;
import dev.pk7r.spigot.starter.core.util.BeanUtil;
import dev.pk7r.spigot.starter.core.util.ClassUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Getter
public abstract class AbstractPluginContext implements PluginContext {

    private final UUID id = UUID.randomUUID();

    private final long initializationTime = System.currentTimeMillis();

    private Plugin plugin;

    private final PluginApplication pluginApplication;

    private final BeanFactory beanFactory;

    private final BeanNameStrategy beanNameStrategy;

    private final BeanDefinitionRegistry beanDefinitionRegistry;

    public AbstractPluginContext(ConfigurableApplication application,
                                 Class<? extends ConfigurableApplication> applicationClass) {
        this(application, applicationClass, new DefaultBeanNameStrategy());
    }

    public AbstractPluginContext(ConfigurableApplication application,
                                 Class<? extends ConfigurableApplication> applicationClass,
                                 BeanNameStrategy beanNameStrategy) {
        this(application, applicationClass, new DefaultBeanDefinitionRegistry(beanNameStrategy), beanNameStrategy);
    }

    public AbstractPluginContext(ConfigurableApplication application,
                                 Class<? extends ConfigurableApplication> applicationClass,
                                 BeanDefinitionRegistry beanDefinitionRegistry,
                                 BeanNameStrategy beanNameStrategy) {
        this(application, applicationClass, beanDefinitionRegistry, beanNameStrategy, new DefaultBeanFactory(beanDefinitionRegistry, beanNameStrategy));
    }

    public AbstractPluginContext(ConfigurableApplication application,
                                 Class<? extends ConfigurableApplication> applicationClass,
                                 BeanDefinitionRegistry beanDefinitionRegistry,
                                 BeanNameStrategy beanNameStrategy,
                                 BeanFactory beanFactory) {
        synchronized (AbstractPluginContext.class) {
            if (!applicationClass.isAnnotationPresent(PluginApplication.class)) {
                throw new ContextInitializationException("Application class must be annotated with @PluginApplication");
            }
            this.pluginApplication = application.getClass().getAnnotation(PluginApplication.class);
            this.beanFactory = beanFactory;
            this.beanNameStrategy = beanNameStrategy;
            this.beanDefinitionRegistry = beanDefinitionRegistry;
            getBeanDefinitionRegistry().registerSingletonBeanDefinition(PluginContext.class, this);
         }
    }

    @Override
    public void start(Plugin plugin) {
        setPlugin(plugin);
        val pluginApplication = getPluginApplication();
        Predicate<Class<?>> isIncluded = candidate -> !Arrays.asList(pluginApplication.excludeClasses()).contains(candidate);
        val injectables = ClassUtil.scan(
                getPluginApplication().basePackages(),
                getPluginApplication().exclude(),
                (BeanUtil::isInjectable),
                getPluginApplication().verbose());
        val injectableBeans = injectables
                .stream()
                .filter(i -> !i.isAnnotation())
                .filter(isIncluded)
                .filter(injectable -> {
                    val annotation = injectable.getAnnotation(AutoConfiguration.class);
                    if (annotation != null) {
                        if (!pluginApplication.enableAutoConfiguration()) {
                            return annotation.force();
                        }
                        return true;
                    }
                    return true;
                })
                .map(injectable -> getBeanDefinitionRegistry().registerBeanDefinition(getBeanFactory(), injectable))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        injectableBeans
                .stream()
                .filter(BeanDefinition::isAutoConfiguration)
                .forEach(definition -> definition.construct(getBeanFactory()));
        val loadedEvent = new ContextLoadedEvent(this);
        getPlugin().getServer().getPluginManager().callEvent(loadedEvent);
        injectableBeans
                .stream()
                .filter(b -> !b.isAutoConfiguration())
                .forEach(definition -> definition.construct(getBeanFactory()));
        val startedEvent = new ContextStartedEvent(this);
        getPlugin().getServer().getPluginManager().callEvent(startedEvent);
        val startupTime = System.currentTimeMillis();
       log.info("Context started after {} ms", startupTime - getInitializationTime());
    }

    @Override
    public void close() {
        val closeEvent = new ContextCloseEvent(this);
        getPlugin().getServer().getPluginManager().callEvent(closeEvent);
        getBeanDefinitionRegistry().getBeanDefinitions().forEach(b -> b.preDestroy(getBeanFactory()));
        getBeanDefinitionRegistry().getBeanDefinitions().clear();
    }

    @Override
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
}