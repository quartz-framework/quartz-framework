package xyz.quartzframework.core.context;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import xyz.quartzframework.core.QuartzApplication;
import xyz.quartzframework.core.QuartzPlugin;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.definition.DefaultBeanDefinitionBuilder;
import xyz.quartzframework.core.bean.definition.PluginBeanDefinition;
import xyz.quartzframework.core.bean.definition.PluginBeanDefinitionBuilder;
import xyz.quartzframework.core.bean.definition.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.bean.definition.metadata.TypeMetadata;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.bean.strategy.BeanNameStrategy;
import xyz.quartzframework.core.condition.Evaluators;
import xyz.quartzframework.core.context.annotation.Configurer;
import xyz.quartzframework.core.exception.ContextInitializationException;
import xyz.quartzframework.core.util.BeanUtil;

import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
@Getter
@NoProxy
public abstract class AbstractQuartzContext<T> implements QuartzContext<T> {

    private final UUID id = UUID.randomUUID();

    private final long initializationTime = System.currentTimeMillis();

    private final QuartzApplication quartzApplication;

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

    @Setter
    private PluginBeanDefinitionBuilder beanDefinitionBuilder;

    public AbstractQuartzContext(Class<? extends QuartzPlugin<T>> pluginClass,
                                 @Nullable PluginBeanDefinitionRegistry beanDefinitionRegistry,
                                 @Nullable DefaultBeanDefinitionBuilder beanDefinitionBuilder,
                                 @Nullable BeanNameStrategy beanNameStrategy,
                                 @Nullable PluginBeanFactory beanFactory,
                                 @Nullable URLClassLoader classLoader) {
        synchronized (AbstractQuartzContext.class) {
            if (!pluginClass.isAnnotationPresent(QuartzApplication.class)) {
                throw new ContextInitializationException("Application class must be annotated with @QuartzApplication");
            }
            this.quartzApplication = pluginClass.getAnnotation(QuartzApplication.class);
            this.beanFactory = beanFactory;
            this.beanNameStrategy = beanNameStrategy;
            this.beanDefinitionBuilder = beanDefinitionBuilder;
            this.beanDefinitionRegistry = beanDefinitionRegistry;
            this.classLoader = classLoader;
            this.pluginClass = pluginClass;
         }
    }

    @Override
    public void start(QuartzPlugin<T> quartzPlugin) {
        setQuartzPlugin(quartzPlugin);
        performInitializationChecks();
        registerDefaultBeans();
        scanAndRegisterInjectables();
        logActiveProfiles();
        phase(PluginBeanDefinition::isConfigurer,
                (b) ->
                        !b.isInitialized() &&
                                !b.isAspect() &&
                                !b.isBootstrapper() &&
                                !b.isContextBootstrapper(),
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isAspect,
                (b) ->
                        !b.isInitialized() &&
                        !b.isContextBootstrapper() &&
                        !b.isBootstrapper() &&
                        !b.isConfigurer(),
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isContextBootstrapper,
                (b) ->
                        !b.isInitialized() &&
                        !b.isAspect() &&
                        !b.isBootstrapper() &&
                        !b.isConfigurer(),
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isBootstrapper,
                (b) ->
                        !b.isInitialized() &&
                        !b.isAspect() &&
                        !b.isConfigurer() &&
                        !b.isContextBootstrapper(),
                (b) -> b.construct(getBeanFactory()));
        phase(b -> !b.isInitialized(),
                (b) ->
                        !b.isInitialized() &&
                        !b.isBootstrapper() &&
                        !b.isAspect() &&
                        !b.isConfigurer() &&
                        !b.isContextBootstrapper(),
                (b) -> b.construct(getBeanFactory()));
        phase(PluginBeanDefinition::isInitialized,
                PluginBeanDefinition::isInjected,
                b -> b.triggerStartMethods(getBeanFactory()));
        logStartupTime();
    }

    @Override
    public void close() {
        phase(PluginBeanDefinition::isInitialized,
                PluginBeanDefinition::isInjected,
                b -> b.preDestroy(getBeanFactory()));
        getBeanDefinitionRegistry().getBeanDefinitions().clear();
    }

    private void scanAndRegisterInjectables() {
        val packages = Stream
                .concat(Arrays.stream(getQuartzApplication().basePackages()), Stream.of(pluginClass.getPackageName()))
                .toArray(String[]::new);
        Predicate<TypeMetadata> isIncluded = candidate -> Arrays
                .stream(quartzApplication.excludeClasses())
                .map(c -> TypeMetadata.of(c, classLoader))
                .noneMatch(c -> candidate.getFullName().equals(c.getFullName())) && !candidate.isAnnotation();
        val scan = TypeMetadata.scan(packages,
                getQuartzApplication().exclude(),
                (BeanUtil::isInjectable),
                (b -> getBeanDefinitionBuilder().isInjectable(b) && isIncluded.test(b)),
                isVerbose(), classLoader)
                .stream()
                .toList();
        val injectables = new ArrayList<>(scan);
        val discovery = injectables
                .stream()
                .map(getBeanDefinitionBuilder()::discovery)
                .filter(s -> s.length > 0)
                .flatMap(Arrays::stream)
                .filter(s -> !Arrays.asList(quartzApplication.basePackages()).contains(s))
                .toArray(String[]::new);
        val mainDiscovery = getBeanDefinitionBuilder().discovery(TypeMetadata.of(getPluginClass(), classLoader));
        val discoverResult = TypeMetadata.scan(
                Stream.concat(Arrays.stream(mainDiscovery), Arrays.stream(discovery)).toArray(String[]::new),
                getQuartzApplication().exclude(),
                (BeanUtil::isInjectable),
                (b -> getBeanDefinitionBuilder().isInjectable(b) && isIncluded.test(b) && !injectables.contains(b)),
                isVerbose(), classLoader)
                .stream()
                .toList();
        injectables.addAll(discoverResult);
        val imports = injectables
                .stream()
                .map(getBeanDefinitionBuilder()::getImports)
                .flatMap(Collection::stream)
                .filter(i -> !injectables.contains(i))
                .toList();
        injectables.addAll(imports);
        log.info("Scan found {} classes and took {} ms", injectables.size(), ((System.currentTimeMillis() - initializationTime)));
        injectables
                .stream()
                .distinct()
                .filter(injectable -> {
                    if (getBeanDefinitionBuilder().isConfigurer(injectable)) {
                        if (!quartzApplication.enableConfigurers()) {
                            return injectable.getAnnotation(Configurer.class)
                                    .map(a -> a.getAttribute("force", boolean.class))
                                    .orElse(false);
                        }
                        return true;
                    }
                    return true;
                })
                .filter(injectable -> {
                    if (getBeanDefinitionBuilder().isContextBootstrapper(injectable)) {
                        if (!injectable.getPackageName().startsWith(TypeMetadata.INTERNAL_PACKAGE + ".")) {
                            log.warn("Class {} is annotated with @ContextBootstrapper but is not in an internal package — ignoring", injectable.getSimpleName());
                            return false;
                        }
                    }
                    return true;
                })
                .map(metadata -> getBeanDefinitionBuilder().create(metadata))
                .filter(Objects::nonNull)
                .peek(injectable -> getBeanDefinitionRegistry().registerBeanDefinition(injectable.getName(), injectable))
                .forEach(definition -> definition.provideMethods(getBeanDefinitionRegistry(), getBeanDefinitionBuilder(), getBeanNameStrategy()));
    }

    private void logActiveProfiles() {
        val profiles = Evaluators.getActiveProfiles().apply(getBeanFactory());
        val join = String.join(", ", profiles);
        log.info("Using '{}' environments", join);
    }

    private void phase(Predicate<PluginBeanDefinition> phaseFilter, Predicate<PluginBeanDefinition> filter, Consumer<PluginBeanDefinition> phase) {
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .sorted(Comparator.comparingInt(PluginBeanDefinition::getOrder).reversed())
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
        log.info("Starting '{}' context...", getId());
        if (getBeanDefinitionRegistry() == null) {
            throw new ContextInitializationException("Can not start a context without a Bean definition registry.");
        }
        if (getBeanDefinitionBuilder() == null) {
            throw new ContextInitializationException("Can not start a context without a Bean definition builder.");
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
        registerSingleton(URLClassLoader.class, getClassLoader());
        registerSingleton(QuartzPlugin.class, this.getQuartzPlugin());
        registerSingleton(getPluginClass(), this.getQuartzPlugin());
        registerSingleton(QuartzContext.class, this);
        registerSingleton(AbstractQuartzContext.class, this);
        registerSingleton(getClass(), this);
    }
    
    @Override
    public void registerSingleton(Object instance) {
        registerSingleton(instance.getClass(), instance);
    }

    @Override
    public void registerSingleton(Class<?> clazz, Object instance) {
        val typeMetadata = TypeMetadata.raw(clazz, classLoader);
        val beanName = beanNameStrategy.generateBeanName(typeMetadata);
        registerSingleton(beanName, clazz, instance);
    }

    @Override
    public void registerSingleton(String beanName, Class<?> clazz, Object instance) {
        val typeMetadata = TypeMetadata.raw(clazz, classLoader);
        registerSingleton(beanName, typeMetadata, instance);
    }

    @Override
    public void registerSingleton(String beanName, TypeMetadata metadata, Object instance) {
        val builder = getBeanDefinitionBuilder();
        val definition = builder.create(metadata);
        val registry = getBeanDefinitionRegistry();
        definition.setSingleton(true);
        registry.updateBeanInstance(definition, instance);
        registry.registerBeanDefinition(beanName, definition);
    }
}