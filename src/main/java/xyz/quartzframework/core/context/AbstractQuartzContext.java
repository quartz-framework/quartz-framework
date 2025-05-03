package xyz.quartzframework.core.context;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import xyz.quartzframework.core.annotation.Configurer;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.PluginApplication;
import xyz.quartzframework.core.QuartzPlugin;
import xyz.quartzframework.core.bean.PluginBeanDefinition;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.condition.Evaluators;
import xyz.quartzframework.core.exception.ContextInitializationException;
import xyz.quartzframework.core.util.BeanUtil;
import xyz.quartzframework.core.util.ClassUtil;

import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
@Getter
@NoProxy
public abstract class AbstractQuartzContext<T> implements QuartzContext<T> {

    private final UUID id = UUID.randomUUID();

    private final long initializationTime = System.currentTimeMillis();

    private final PluginApplication pluginApplication;

    private final Class<? extends QuartzPlugin<T>> pluginClass;

    @Setter
    private URLClassLoader classLoader;

    @Setter
    private QuartzPlugin<T> quartzPlugin;

    @Setter
    private PluginBeanFactory beanFactory;

    @Setter
    private BeanNameStrategy beanNameStrategy;

    @Setter
    private PluginBeanDefinitionRegistry beanDefinitionRegistry;

    public AbstractQuartzContext(Class<? extends QuartzPlugin<T>> pluginClass) {
        this(pluginClass, null, null, null, null);
    }

    public AbstractQuartzContext(Class<? extends QuartzPlugin<T>> pluginClass,
                                 @Nullable PluginBeanDefinitionRegistry beanDefinitionRegistry,
                                 @Nullable BeanNameStrategy beanNameStrategy,
                                 @Nullable PluginBeanFactory beanFactory,
                                 @Nullable URLClassLoader classLoader) {
        synchronized (AbstractQuartzContext.class) {
            if (!pluginClass.isAnnotationPresent(PluginApplication.class)) {
                throw new ContextInitializationException("Application class must be annotated with @PluginApplication");
            }
            this.pluginApplication = pluginClass.getAnnotation(PluginApplication.class);
            this.beanFactory = beanFactory;
            this.beanNameStrategy = beanNameStrategy;
            this.beanDefinitionRegistry = beanDefinitionRegistry;
            this.classLoader = classLoader;
            this.pluginClass = pluginClass;
         }
    }

    @Override
    public void start(QuartzPlugin<T> quartzPlugin) {
        log.info("Starting '{}' context...", getId());
        setQuartzPlugin(quartzPlugin);
        performInitializationChecks();
        registerDefaultBeans();
        scanAndRegisterInjectables();
        validateAndCleanInvalidBeans();
        logActiveProfiles();
        phase(PluginBeanDefinition::isAspect,
                (b) ->
                        !b.isContextBootstrapper() &&
                        !b.isBootstrapper() &&
                        !b.isConfigurer(),
                true,
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isContextBootstrapper,
                (b) ->
                        !b.isAspect() &&
                        !b.isBootstrapper() &&
                        !b.isConfigurer(),
                true,
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isConfigurer,
                (b) ->
                        !b.isAspect() &&
                        !b.isBootstrapper() &&
                        !b.isContextBootstrapper(),
                true,
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isBootstrapper,
                (b) ->
                        !b.isAspect() &&
                        !b.isConfigurer() &&
                        !b.isContextBootstrapper(),
                true,
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isInitialized,
                PluginBeanDefinition::isInjected,
                false,
                b -> b.triggerLoadMethods(getBeanFactory()));
        phase(b -> !b.isInitialized(),
                (b) ->
                        !b.isBootstrapper() &&
                        !b.isAspect() &&
                        !b.isConfigurer() &&
                        !b.isContextBootstrapper(),
                true,
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isInitialized,
                PluginBeanDefinition::isInjected,
                false,
                b -> b.triggerStartMethods(getBeanFactory()));
        logStartupTime();
    }

    @Override
    public void close() {
        phase(PluginBeanDefinition::isInitialized,
                PluginBeanDefinition::isInjected,
                false,
                b -> b.preDestroy(getBeanFactory()));
        getBeanDefinitionRegistry().getBeanDefinitions().clear();
    }

    private void scanAndRegisterInjectables() {
        Predicate<Class<?>> isIncluded = candidate -> !Arrays.asList(pluginApplication.excludeClasses()).contains(candidate) && !candidate.isAnnotation();
        val injectables = ClassUtil.scan(
                getPluginApplication().basePackages(),
                getPluginApplication().exclude(),
                (b -> BeanUtil.isInjectable(b) && isIncluded.test(b)),
                isVerbose());
        val discovery = injectables
                .stream()
                .map(BeanUtil::discovery)
                .filter(s -> s.length > 0)
                .flatMap(Arrays::stream)
                .filter(s -> !Arrays.asList(pluginApplication.basePackages()).contains(s))
                .toArray(String[]::new);
        val mainDiscovery = BeanUtil.discovery(getPluginClass());
        val discoverResult = ClassUtil.scan(
                Stream.concat(Arrays.stream(mainDiscovery), Arrays.stream(discovery)).toArray(String[]::new),
                getPluginApplication().exclude(),
                (b -> BeanUtil.isInjectable(b) && isIncluded.test(b) && !injectables.contains(b)),
                isVerbose());
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
                .toList();
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
                .toList();
        for (val b : invalidBeans) {
            getBeanDefinitionRegistry().unregisterBeanDefinition(b.getId());
        }
    }

    private void logActiveProfiles() {
        val profiles = Evaluators.getActiveProfiles().apply(getBeanFactory());
        val join = String.join(", ", profiles);
        log.info("Loading context {} with '{}' active profiles", getId(), join);
    }

    private void phase(Predicate<PluginBeanDefinition> phaseFilter, Predicate<PluginBeanDefinition> filter, boolean eagerInit, Consumer<PluginBeanDefinition> phase) {
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .filter(b -> (eagerInit && !b.isInitialized()))
                .sorted(Comparator.comparingInt(PluginBeanDefinition::getOrder))
                .filter(pluginBeanDefinition -> !pluginBeanDefinition.isDeferred())
                .filter(phaseFilter)
                .filter(filter)
                .forEach(phase);
    }

    private void logStartupTime() {
        val startupTime = System.currentTimeMillis();
        log.info("Context started after {} ms", startupTime - getInitializationTime());
    }

    private void performInitializationChecks() {
        if (getBeanDefinitionRegistry() == null) {
            throw new ContextInitializationException("Can not start a context without a Bean definition registry.");
        }
        if (getBeanFactory() == null) {
            throw new ContextInitializationException("Can not start a context without a Bean factory.");
        }
        if (getBeanNameStrategy() == null) {
            throw new ContextInitializationException("Can not start a context without a Bean naming strategy.");
        }
        if (getClassLoader() == null) {
            throw new ContextInitializationException("Can not start a context without a plugin classloader.");
        }
        if (this.getQuartzPlugin() == null) {
            throw new ContextInitializationException("Can not start a context without a quartz plugin.");
        }
    }

    private void registerDefaultBeans() {
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(URLClassLoader.class, getClassLoader());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(QuartzPlugin.class, this.getQuartzPlugin());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getPluginClass(), this.getQuartzPlugin());
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(QuartzContext.class, this);
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(AbstractQuartzContext.class, this);
        getBeanDefinitionRegistry().registerSingletonBeanDefinition(getClass(), this);
    }
}