package xyz.quartzframework.core.condition;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.plugin.Plugin;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.property.PropertyPostProcessor;
import xyz.quartzframework.core.util.ClassUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public final class Evaluators {

    private static final Map<PluginBeanFactory, List<String>> ACTIVE_PROFILES_CACHE = new WeakHashMap<>();

    public static final String DEFAULT_PROFILE = "default";

    public static final List<String> DEFAULT_PROFILES = Collections.singletonList(DEFAULT_PROFILE);

    private static final Map<ConditionType, ConditionEvaluator> EVALUATORS = buildEvaluators();

    private Map<ConditionType, ConditionEvaluator> buildEvaluators() {
        Map<ConditionType, ConditionEvaluator> evaluators = new HashMap<>();
        evaluators.put(ConditionType.CONDITIONAL, (def, factory) -> {
            val cond = def.getGenericConditionMetadata();
            return cond == null || factory.getBean(cond.getValue()).test();
        });
        evaluators.put(ConditionType.ON_CLASS, (def, factory) -> {
            val metadata = def.getClassConditionMetadata();
            return metadata == null || metadata.getClassNames().stream().allMatch(n -> ClassUtil.isClassLoaded(n, factory.getClassLoader()));
        });
        evaluators.put(ConditionType.ON_MISSING_CLASS, (def, factory) -> {
            val metadata = def.getMissingClassConditionMetadata();
            return metadata == null || metadata.getClassNames().stream().noneMatch(n -> ClassUtil.isClassLoaded(n, factory.getClassLoader()));
        });
        evaluators.put(ConditionType.ON_BEAN, (def, factory) -> {
            val metadata = def.getBeanConditionMetadata();
            return metadata == null || Arrays
                    .stream(metadata.getClasses())
                    .noneMatch(req -> factory.getRegistry().getBeanDefinitionsByType(req).isEmpty());
        });
        evaluators.put(ConditionType.ON_MISSING_BEAN, (def, factory) -> {
            val metadata = def.getMissingBeanConditionMetadata();
            if (metadata == null) {
                return true;
            }
            val requiredTypes = Arrays.asList(metadata.getClasses());
            return requiredTypes.stream()
                    .noneMatch(requiredType -> factory
                            .getRegistry()
                            .getBeanDefinitions()
                            .stream()
                            .filter(b -> !b.getId().equals(def.getId()))
                            .anyMatch(factory.getRegistry().filterBeanDefinition(requiredType)));
        });
        evaluators.put(ConditionType.ON_PROPERTY, (def, factory) -> {
            val metadata = def.getPropertyConditionMetadata();
            if (metadata == null) return true;
            val env = factory.getBean(PropertyPostProcessor.class);
            val value = env.process(metadata.getProperty().value(), metadata.getProperty().source(), String.class);
            return Objects.equals(value, metadata.getExpected());
        });
        evaluators.put(ConditionType.ON_ENVIRONMENT, (def, factory) -> {
            val environments = def.getEnvironments();
            if (environments == null || environments.isEmpty() || (environments.size() == 1 && environments.get(0).equalsIgnoreCase("default"))) return true;
            val profilesActive = getActiveProfiles().apply(factory);
            for (String environment : environments) {
                boolean negate = environment.startsWith("!");
                String profile = negate ? environment.substring(1) : environment;
                boolean active = profilesActive.contains(profile);
                if (negate && active) return false;
                if (!negate && !active) return false;
            }
            return true;
        });
        evaluators.put(ConditionType.ON_ANNOTATION, (def, factory) -> {
            val metadata = def.getAnnotationConditionMetadata();
            if (metadata == null) return true;
            return Arrays.stream(metadata.getClasses()).noneMatch(a -> factory.getBeansWithAnnotation(a).isEmpty());
        });
        return evaluators;
    }

    public Map<ConditionType, ConditionEvaluator> getEvaluators() {
        return EVALUATORS;
    }

    public Function<PluginBeanFactory, List<String>> getActiveProfiles() {
        return factory -> ACTIVE_PROFILES_CACHE.computeIfAbsent(factory, f -> {
            val plugin = f.getBean(Plugin.class);
            val env = f.getBean(PropertyPostProcessor.class);
            val profileInVariables = env
                    .getEnvironmentVariables()
                    .getOrDefault(String.format("%S_PLUGIN_PROFILES", plugin.getName().toUpperCase()), "default");
            val profilesActive = Arrays.stream(profileInVariables.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(ArrayList::new));
            if (profilesActive.isEmpty()) {
                profilesActive.add("default");
            }
            return profilesActive;
        });
    }
}
