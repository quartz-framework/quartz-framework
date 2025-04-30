package xyz.quartzframework.core.context;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import xyz.quartzframework.core.annotation.Configurer;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.PluginApplication;
import xyz.quartzframework.core.application.ConfigurableApplication;
import xyz.quartzframework.core.bean.PluginBeanDefinition;
import xyz.quartzframework.core.bean.factory.DefaultPluginBeanFactory;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.registry.DefaultPluginBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.bean.strategy.DefaultBeanNameStrategy;
import xyz.quartzframework.core.condition.Evaluators;
import xyz.quartzframework.core.event.*;
import xyz.quartzframework.core.exception.ContextInitializationException;
import xyz.quartzframework.core.util.BeanUtil;
import xyz.quartzframework.core.util.ClassUtil;

import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Getter
@NoProxy
public abstract class AbstractPluginContext implements PluginContext {

    private final UUID id = UUID.randomUUID();

    private final long initializationTime = System.currentTimeMillis();

    private Plugin plugin;

    private final URLClassLoader classLoader;

    private final PluginApplication pluginApplication;

    private final PluginBeanFactory beanFactory;

    private final ConfigurableApplication configurableApplication;

    private final Class<? extends ConfigurableApplication> applicationClass;

    private final BeanNameStrategy beanNameStrategy;

    private final PluginBeanDefinitionRegistry beanDefinitionRegistry;

    public AbstractPluginContext(ConfigurableApplication application,
                                 Class<? extends ConfigurableApplication> applicationClass,
                                 URLClassLoader classLoader) {
        this(application, applicationClass, new DefaultBeanNameStrategy(), classLoader);
    }

    public AbstractPluginContext(ConfigurableApplication application,
                                 Class<? extends ConfigurableApplication> applicationClass,
                                 BeanNameStrategy beanNameStrategy,
                                 URLClassLoader classLoader) {
        this(application, applicationClass, new DefaultPluginBeanDefinitionRegistry(beanNameStrategy), beanNameStrategy, classLoader);
    }

    public AbstractPluginContext(ConfigurableApplication application,
                                 Class<? extends ConfigurableApplication> applicationClass,
                                 PluginBeanDefinitionRegistry beanDefinitionRegistry,
                                 BeanNameStrategy beanNameStrategy,
                                 URLClassLoader classLoader) {
        this(application, applicationClass, beanDefinitionRegistry, beanNameStrategy, new DefaultPluginBeanFactory(classLoader, beanDefinitionRegistry, beanNameStrategy), classLoader);
    }

    public AbstractPluginContext(ConfigurableApplication application,
                                 Class<? extends ConfigurableApplication> applicationClass,
                                 PluginBeanDefinitionRegistry beanDefinitionRegistry,
                                 BeanNameStrategy beanNameStrategy,
                                 PluginBeanFactory beanFactory,
                                 URLClassLoader classLoader) {
        synchronized (AbstractPluginContext.class) {
            if (!applicationClass.isAnnotationPresent(PluginApplication.class)) {
                throw new ContextInitializationException("Application class must be annotated with @PluginApplication");
            }
            this.pluginApplication = application.getClass().getAnnotation(PluginApplication.class);
            this.beanFactory = beanFactory;
            this.beanNameStrategy = beanNameStrategy;
            this.beanDefinitionRegistry = beanDefinitionRegistry;
            this.classLoader = classLoader;
            this.applicationClass = applicationClass;
            this.configurableApplication = application;
            getBeanDefinitionRegistry().registerSingletonBeanDefinition(URLClassLoader.class, classLoader);
            getBeanDefinitionRegistry().registerSingletonBeanDefinition(ConfigurableApplication.class, application);
            getBeanDefinitionRegistry().registerSingletonBeanDefinition(applicationClass, application);
            getBeanDefinitionRegistry().registerSingletonBeanDefinition(PluginContext.class, this);
            getBeanDefinitionRegistry().registerSingletonBeanDefinition(AbstractPluginContext.class, this);
         }
    }

    @Override
    public void start(Plugin plugin) {
        log.info("Starting {} context...", plugin.getName());
        setPlugin(plugin);
        scanAndRegisterInjectables();
        validateAndCleanInvalidBeans();
        logActiveProfiles();
        constructPhase(PluginBeanDefinition::isAspect,
                (b) ->
                        !b.isContextBootstrapper() &&
                        !b.isBootstrapper() &&
                        !b.isConfigurer(),
                "Aspect Phase");
        constructPhase(PluginBeanDefinition::isContextBootstrapper,
                (b) ->
                        !b.isAspect() &&
                        !b.isBootstrapper() &&
                        !b.isConfigurer(),
                "Context Bootstrap Phase");
        constructPhase(PluginBeanDefinition::isConfigurer,
                (b) ->
                        !b.isAspect() &&
                        !b.isBootstrapper() &&
                        !b.isContextBootstrapper(),
                "Context Configurers Phase");
        constructPhase(PluginBeanDefinition::isBootstrapper,
                (b) ->
                        !b.isAspect() &&
                        !b.isConfigurer() &&
                        !b.isContextBootstrapper(),
                "Bootstrap Phase");
        fireContextEvent(new ContextLoadedEvent(this));
        constructPhase(b -> !b.isInitialized(),
                (b) ->
                        !b.isBootstrapper() &&
                        !b.isAspect() &&
                        !b.isConfigurer() &&
                        !b.isContextBootstrapper(),
                "Main Phase");
        fireContextEvent(new ContextStartedEvent(this));
        logStartupTime();
    }

    @Override
    public void close() {
        fireContextEvent(new ContextCloseEvent(this));
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .filter(PluginBeanDefinition::isInjected)
                .forEach(b -> b.preDestroy(getBeanFactory()));
        getBeanDefinitionRegistry().getBeanDefinitions().clear();
        HandlerList.unregisterAll(getPlugin());
    }

    @Override
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    private void scanAndRegisterInjectables() {
        Predicate<Class<?>> isIncluded = candidate -> !Arrays.asList(pluginApplication.excludeClasses()).contains(candidate) && !candidate.isAnnotation();
        val injectables = ClassUtil.scan(
                getPluginApplication().basePackages(),
                getPluginApplication().exclude(),
                (b -> BeanUtil.isInjectable(b) && isIncluded.test(b)),
                getPluginApplication().verbose());
        val discovery = injectables
                .stream()
                .map(BeanUtil::discovery)
                .filter(s -> s.length > 0)
                .flatMap(Arrays::stream)
                .filter(s -> !Arrays.asList(pluginApplication.basePackages()).contains(s))
                .toArray(String[]::new);
        val mainDiscovery = BeanUtil.discovery(getApplicationClass());
        val discoverResult = ClassUtil.scan(
                Stream.concat(Arrays.stream(mainDiscovery), Arrays.stream(discovery)).toArray(String[]::new),
                getPluginApplication().exclude(),
                (b -> BeanUtil.isInjectable(b) && isIncluded.test(b) && !injectables.contains(b)),
                getPluginApplication().verbose());
        injectables.addAll(discoverResult);
        val imports = injectables
                .stream()
                .map(BeanUtil::getImports)
                .flatMap(Collection::stream)
                .filter(i -> !injectables.contains(i))
                .peek(c -> {
                    if (!ClassUtil.isClassLoaded(c.getName(), classLoader)) {
                        try {
                            classLoader.loadClass(c.getName());
                        } catch (ClassNotFoundException e) {
                            throw new ContextInitializationException("Failed to import " + c.getName(), e);
                        }
                    }
                })
                .collect(Collectors.toList());
        injectables.addAll(imports);
        log.info("Scan found {} classes and took {} ms", injectables.size(), ((System.currentTimeMillis() - initializationTime)));
        injectables
                .stream()
                .filter(injectable -> {
                    val annotation = injectable.getAnnotation(Configurer.class);
                    if (annotation != null) {
                        if (!pluginApplication.enableConfigurers()) {
                            return annotation.force();
                        }
                        return true;
                    }
                    return true;
                })
                .forEach(injectable -> getBeanDefinitionRegistry().defineBeans(injectable));
    }

    private void validateAndCleanInvalidBeans() {
        val invalidBeans = getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .filter(b -> !b.isValid(getBeanFactory()))
                .collect(Collectors.toList());
        for (val b : invalidBeans) {
            getBeanDefinitionRegistry().unregisterBeanDefinition(b.getId());
        }
    }

    private void logActiveProfiles() {
        val profiles = Evaluators.getActiveProfiles().apply(getBeanFactory());
        val join = String.join(", ", profiles);
        log.info("Loading context {} with '{}' active profiles", getId(), join);
    }

    private void constructPhase(Predicate<PluginBeanDefinition> phaseFilter, Predicate<PluginBeanDefinition> filter, String phaseName) {
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .filter(b -> !b.isInitialized())
                .sorted(Comparator.comparingInt(PluginBeanDefinition::getOrder))
                .filter(pluginBeanDefinition -> !pluginBeanDefinition.isDeferred())
                .filter(phaseFilter)
                .filter(filter)
                .forEach(definition -> definition.construct(getBeanFactory()));
    }

    private void fireContextEvent(ContextEvent event) {
        val publisher = getBeanFactory().getBean(EventPublisher.class);
        publisher.publish(event, false, false);
    }

    private void logStartupTime() {
        val startupTime = System.currentTimeMillis();
        log.info("Context started after {} ms", startupTime - getInitializationTime());
    }
}