package dev.pk7r.spigot.starter.core;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.PluginApplication;
import dev.pk7r.spigot.starter.core.context.PluginContext;
import dev.pk7r.spigot.starter.core.convert.*;
import dev.pk7r.spigot.starter.core.property.PropertyPostProcessor;
import dev.pk7r.spigot.starter.core.exception.ContextInitializationException;
import dev.pk7r.spigot.starter.core.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.factory.CommandFactory;
import dev.pk7r.spigot.starter.core.factory.EventFactory;
import dev.pk7r.spigot.starter.core.factory.PropertySourceFactory;
import dev.pk7r.spigot.starter.core.injector.BeanInjector;
import dev.pk7r.spigot.starter.core.lifecycle.LifecycleMethodsInspector;
import dev.pk7r.spigot.starter.core.registry.BeanDefinitionRegistry;
import dev.pk7r.spigot.starter.core.util.BeanUtil;
import dev.pk7r.spigot.starter.core.util.ClassUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@Getter
@Setter(AccessLevel.PRIVATE)
public abstract class DefaultPluginContext implements PluginContext {

    private final UUID id = UUID.randomUUID();

    private final long initializationTime = System.currentTimeMillis();

    private final long startupTime;

    private Plugin plugin;

    private String pluginName;

    private PluginApplication pluginApplication;

    private BeanFactory beanFactory;

    private EventFactory eventFactory;

    private CommandFactory commandFactory;

    private PaperCommandManager commandManager;

    private PropertyPostProcessor propertyPostProcessor;

    private PropertySourceFactory propertySourceFactory;

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
            val pluginApplicationAnnotation = application.getClass().getAnnotation(PluginApplication.class);
            setPlugin(application);
            setPluginApplication(pluginApplicationAnnotation);
            setPluginName(application.getName());
            setBeanDefinitionRegistry(new DefaultBeanDefinitionRegistry(this));
            setBeanFactory(new DefaultBeanFactory(getBeanDefinitionRegistry()));
            setBeanInjector(new DefaultBeanInjector(getBeanFactory()));
            setLifecycleMethodsInspector(new DefaultLifecycleMethodsInspector(getBeanFactory()));
            setEventFactory(new DefaultEventFactory(getPlugin()));
            setCommandManager(new PaperCommandManager(getPlugin()));
            setCommandFactory(new DefaultCommandFactory(getPlugin(), getCommandManager()));
            setPropertySourceFactory(new DefaultPropertySourceFactory(getPlugin()));
            setPropertyPostProcessor(new DefaultPropertyPostProcessor(getPropertySourceFactory(), getBeanFactory()));
            registerDefaultBeans();
            startContext();
            this.startupTime = System.currentTimeMillis();
            afterStart.accept(this);
            log.info("Context {} initialized after {} ms", getId(), getStartupTime() - getInitializationTime());
        }
    }

    @Override
    public void startContext() {
        val scanner = DefaultClassScanner.getInstance();
        val pluginApplication = getPluginApplication();
        val autoConfigs = pluginApplication.enableAutoConfiguration() ?
                scanner.scan(
                        getPluginApplication().basePackages(),
                        getPluginApplication().exclude(),
                        clazz -> clazz.isAnnotationPresent(AutoConfiguration.class),
                        getPluginApplication().verbose()
                ) : Collections.<Class<?>>emptyList();
        val injectables = DefaultClassScanner.getInstance().scan(
                getPluginApplication().basePackages(),
                getPluginApplication().exclude(),
                (BeanUtil::isInjectable),
                getPluginApplication().verbose());
        injectables.addAll(autoConfigs);
        Predicate<Class<?>> isIncluded = candidate -> !Arrays.asList(pluginApplication.excludeClasses()).contains(candidate);
        injectables
                .stream()
                .filter(isIncluded)
                .filter(clazz -> !clazz.isAnnotation())
                .filter(injectable -> {
                    if (BeanUtil.isConditionalOnClass(injectable)) {
                        val classNames = BeanUtil.getConditionalOnClass(injectable);
                        return classNames.stream().anyMatch(ClassUtil::isClassLoaded);
                    } else return true;
                })
                .forEach(injectable -> getBeanDefinitionRegistry().registerBeanDefinition(injectable));
        val verbose = pluginApplication.verbose();
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .peek(beanDefinition -> getLifecycleMethodsInspector().registerLifecycleMethods(beanDefinition.getLiteralType()))
                .peek(beanDefinition -> getBeanInjector().inject(beanDefinition.getLiteralType()))
                .peek(beanDefinition -> {
                    val instance = beanDefinition.getInstance();
                    if (instance instanceof BaseCommand) {
                        val command = (BaseCommand) instance;
                        getCommandFactory().registerCommand(command);
                    }
                })
                .peek(beanDefinition -> {
                    val instance = beanDefinition.getInstance();
                    if (instance instanceof Listener) {
                        val listener = (Listener) instance;
                        getEventFactory().registerEvents(listener);
                    }
                })
                .forEach(beanDefinition -> {
                    val postConstructMethods = getLifecycleMethodsInspector().getPostConstructMethods();
                    postConstructMethods
                            .stream()
                            .filter(m -> m.getDeclaringClass().equals(beanDefinition.getLiteralType()))
                            .findFirst()
                            .ifPresent(postConstructMethod -> getLifecycleMethodsInspector().invokeLifecycleMethod(postConstructMethod));
                });
        val listeners = getBeanFactory().getBeansOfType(Listener.class);
        if (verbose) log.info("Registered {} listeners", listeners.size());
        if (verbose) log.info("Registered {} commands", getCommandFactory().getCommandManager().getRegisteredRootCommands().size());
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

    protected void registerDefaultBeans() {
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPropertySourceFactory());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPropertyPostProcessor());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "Plugin", Plugin.class, getPlugin());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "PluginManager", PluginManager.class, getPlugin().getServer().getPluginManager());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "PluginLoader", PluginLoader.class, getPlugin().getPluginLoader());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "Server", Server.class, getPlugin().getServer());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "Scheduler", BukkitScheduler.class, getPlugin().getServer().getScheduler());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "Context", PluginContext.class, this);
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "BeanFactory", BeanFactory.class, getBeanFactory());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "BeanDefinitionRegistry", BeanDefinitionRegistry.class, getBeanDefinitionRegistry());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "BeanInjector", BeanInjector.class, getBeanInjector());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "LifecycleMethodsInspector", LifecycleMethodsInspector.class, getLifecycleMethodsInspector());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "EventFactory", EventFactory.class, getEventFactory());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "CommandFactory", CommandFactory.class, getCommandFactory());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginName() + "CommandManager", PaperCommandManager.class, getCommandManager());
        registerConverters();
    }

    protected void registerConverters() {
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new BigDecimalConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new BigIntegerConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new BooleanConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new DoubleConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new DurationConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new FloatConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new InstantConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new IntegerConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new LocalDateConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new LocalDateTimeConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new LocalTimeConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new StringListConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new StringConvertService());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(new LongConvertService());
    }
}