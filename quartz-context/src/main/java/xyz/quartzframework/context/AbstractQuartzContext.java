package xyz.quartzframework.context;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import xyz.quartzframework.Quartz;
import xyz.quartzframework.QuartzPlugin;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.condition.Evaluators;
import xyz.quartzframework.beans.definition.QuartzBeanDefinition;
import xyz.quartzframework.beans.definition.QuartzBeanDefinitionBuilder;
import xyz.quartzframework.beans.definition.QuartzBeanDefinitionRegistry;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.beans.strategy.BeanNameStrategy;
import xyz.quartzframework.beans.support.BeanUtil;
import xyz.quartzframework.stereotype.Configurer;

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

    private final QuartzPlugin informationMetadata;

    private final Class<? extends Quartz<T>> pluginClass;

    @Setter
    private URLClassLoader classLoader;

    @Setter
    private Quartz<T> quartz;

    @Setter
    private QuartzBeanFactory beanFactory;

    @Setter
    private BeanNameStrategy beanNameStrategy;

    @Setter
    private QuartzBeanDefinitionRegistry beanDefinitionRegistry;

    @Setter
    private QuartzBeanDefinitionBuilder beanDefinitionBuilder;

    public AbstractQuartzContext(Class<? extends Quartz<T>> pluginClass,
                                 @Nullable QuartzBeanDefinitionRegistry beanDefinitionRegistry,
                                 @Nullable QuartzBeanDefinitionBuilder beanDefinitionBuilder,
                                 @Nullable BeanNameStrategy beanNameStrategy,
                                 @Nullable QuartzBeanFactory beanFactory,
                                 @Nullable URLClassLoader classLoader) {
        synchronized (AbstractQuartzContext.class) {
            if (!pluginClass.isAnnotationPresent(QuartzPlugin.class)) {
                throw new ContextInitializationException("Application class must be annotated with @QuartzPlugin");
            }
            this.informationMetadata = pluginClass.getAnnotation(QuartzPlugin.class);
            this.beanFactory = beanFactory;
            this.beanNameStrategy = beanNameStrategy;
            this.beanDefinitionBuilder = beanDefinitionBuilder;
            this.beanDefinitionRegistry = beanDefinitionRegistry;
            this.classLoader = classLoader;
            this.pluginClass = pluginClass;
         }
    }

    @Override
    public void start(Quartz<T> quartz) {
        setQuartz(quartz);
        performInitializationChecks();
        registerDefaultBeans();
        scanAndRegisterInjectables();
        logActiveProfiles();
        phase(QuartzBeanDefinition::isConfigurer,
                (b) ->
                        !b.isInitialized() &&
                                !b.isAspect() &&
                                !b.isBootstrapper() &&
                                !b.isContextBootstrapper(),
                (b) -> b.construct(getBeanFactory()));
        phase(QuartzBeanDefinition::isAspect,
                (b) ->
                        !b.isInitialized() &&
                        !b.isContextBootstrapper() &&
                        !b.isBootstrapper() &&
                        !b.isConfigurer(),
                (b) -> b.construct(getBeanFactory()));
        phase(QuartzBeanDefinition::isContextBootstrapper,
                (b) ->
                        !b.isInitialized() &&
                        !b.isAspect() &&
                        !b.isBootstrapper() &&
                        !b.isConfigurer(),
                (b) -> b.construct(getBeanFactory()));
        phase(QuartzBeanDefinition::isBootstrapper,
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
        phase(QuartzBeanDefinition::isInitialized,
                QuartzBeanDefinition::isInjected,
                b -> b.triggerMethods(getBeanFactory(), (m) -> {
                    if (!m.hasAnnotation(ContextLoads.class)) {
                        return false;
                    }
                    if (!m.isVoid()) {
                        log.warn("Ignoring @ContextLoads method '{}' in '{}' – must return void.",
                                m.getName(), b.getTypeMetadata().getFullName());
                        return false;
                    }
                    return true;
                }));
        logStartupTime();
    }

    @Override
    public void close() {
        phase(QuartzBeanDefinition::isInitialized,
                QuartzBeanDefinition::isInjected,
                b -> b.preDestroy(getBeanFactory()));
        getBeanDefinitionRegistry().getBeanDefinitions().clear();
    }

    private void scanAndRegisterInjectables() {
        val packages = Stream
                .concat(Arrays.stream(getInformationMetadata().basePackages()), Stream.of(pluginClass.getPackageName()))
                .toArray(String[]::new);
        Predicate<TypeMetadata> isIncluded = candidate -> Arrays
                .stream(informationMetadata.excludeClasses())
                .map(c -> TypeMetadata.of(c, classLoader))
                .noneMatch(c -> candidate.getFullName().equals(c.getFullName())) && !candidate.isAnnotation();
        val scan = TypeMetadata.scan(packages,
                getInformationMetadata().exclude(),
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
                .filter(s -> !Arrays.asList(informationMetadata.basePackages()).contains(s))
                .toArray(String[]::new);
        val mainDiscovery = getBeanDefinitionBuilder().discovery(TypeMetadata.of(getPluginClass(), classLoader));
        val discoverResult = TypeMetadata.scan(
                Stream.concat(Arrays.stream(mainDiscovery), Arrays.stream(discovery)).toArray(String[]::new),
                getInformationMetadata().exclude(),
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
                        if (!informationMetadata.enableConfigurers()) {
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
                .peek(definition -> getBeanDefinitionRegistry().registerBeanDefinition(definition.getName(), definition))
                .forEach(definition -> definition.provideMethods(getBeanDefinitionRegistry(), getBeanDefinitionBuilder(), getBeanNameStrategy()));
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .filter(def -> !def.isValid(getBeanFactory()))
                .forEach(def -> getBeanDefinitionRegistry().unregisterBeanDefinition(def.getId()));
    }

    private void logActiveProfiles() {
        val profiles = Evaluators.getActiveProfiles().apply(getBeanFactory());
        val join = String.join(", ", profiles);
        log.info("Using '{}' environments", join);
    }

    private void phase(Predicate<QuartzBeanDefinition> phaseFilter, Predicate<QuartzBeanDefinition> filter, Consumer<QuartzBeanDefinition> phase) {
        getBeanDefinitionRegistry()
                .getBeanDefinitions()
                .stream()
                .sorted(Comparator.comparingInt(QuartzBeanDefinition::getOrder).reversed())
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
        if (this.getQuartz() == null) {
            throw new ContextInitializationException("Can not start a context without a quartz plugin.");
        }
    }

    private void registerDefaultBeans() {
        registerSingleton(URLClassLoader.class, getClassLoader());
        registerSingleton(Quartz.class, this.getQuartz());
        registerSingleton(getPluginClass(), this.getQuartz());
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
        if (definition == null) return;
        val registry = getBeanDefinitionRegistry();
        definition.setSingleton(true);
        registry.updateBeanInstance(definition, instance);
        registry.registerBeanDefinition(beanName, definition);
    }
}