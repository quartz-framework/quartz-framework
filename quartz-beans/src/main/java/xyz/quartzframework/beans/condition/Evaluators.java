package xyz.quartzframework.beans.condition;

import lombok.experimental.UtilityClass;
import lombok.val;
import xyz.quartzframework.Quartz;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.config.PropertyPostProcessor;
import xyz.quartzframework.util.ClassUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public final class Evaluators {

    private static final Map<QuartzBeanFactory, List<String>> ACTIVE_PROFILES_CACHE = new WeakHashMap<>();

    public static final String DEFAULT_PROFILE = "default";

    public static final List<String> DEFAULT_PROFILES = Collections.singletonList(DEFAULT_PROFILE);

    private static final Map<ConditionType, ConditionEvaluator> EVALUATORS = buildEvaluators();

    private Map<ConditionType, ConditionEvaluator> buildEvaluators() {
        Map<ConditionType, ConditionEvaluator> evaluators = new HashMap<>();

        evaluators.put(ConditionType.CONDITIONAL, (def, factory) -> {
            val cond = def.getGenericConditionMetadata();
            return cond != null && factory.getBean(cond.getValue()).test();
        });

        evaluators.put(ConditionType.ON_CLASS, (def, factory) -> {
            val metadata = def.getClassConditionMetadata();
            return metadata != null && metadata.getClassNames().stream()
                                .allMatch(n -> ClassUtil.isClassLoaded(n, factory.getClassLoader()));
        });

        evaluators.put(ConditionType.ON_MISSING_CLASS, (def, factory) -> {
            val metadata = def.getMissingClassConditionMetadata();
            return metadata != null && metadata.getClassNames().stream()
                                .noneMatch(n -> ClassUtil.isClassLoaded(n, factory.getClassLoader()));
        });

        evaluators.put(ConditionType.ON_BEAN, (def, factory) -> {
            val metadata = def.getBeanConditionMetadata();
            return metadata != null && metadata.getClassNames()
                    .stream()
                    .allMatch(className ->
                            factory.getRegistry()
                                    .getBeanDefinitions()
                                    .stream()
                                    .anyMatch(b -> b.getTypeMetadata().getRawName().equals(className))
                    );
        });

        evaluators.put(ConditionType.ON_MISSING_BEAN, (def, factory) -> {
            val metadata = def.getMissingBeanConditionMetadata();
            return metadata != null && metadata.getClassNames().stream()
                    .allMatch(className ->
                            factory.getRegistry().getBeanDefinitions()
                                    .stream()
                                    .filter(b -> !b.getId().equals(def.getId()))
                                    .noneMatch(b -> b.getTypeMetadata().getRawName().equals(className))
                    );
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
            if (environments.isEmpty() || environments.size() == 1 && environments.get(0).equalsIgnoreCase(DEFAULT_PROFILE)) {
                return true;
            }
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
            return metadata != null && Arrays.stream(metadata.getClasses())
                    .allMatch(ann ->
                            factory.getRegistry().getBeanDefinitions().stream()
                                    .anyMatch(defn -> defn.getTypeMetadata().hasAnnotation(ann)));
        });
        return evaluators;
    }


    public Map<ConditionType, ConditionEvaluator> getEvaluators() {
        return EVALUATORS;
    }

public Function<QuartzBeanFactory, List<String>> getActiveProfiles() {
        return factory -> ACTIVE_PROFILES_CACHE.computeIfAbsent(factory, f -> {
            val quartz = f.getBean(Quartz.class);
            val env = f.getBean(PropertyPostProcessor.class);
            val profileInVariables = env.getEnvironmentVariables()
                    .getOrDefault(quartz.getName().toUpperCase() + "_PLUGIN_PROFILES", DEFAULT_PROFILE);
            val profilesActive = Arrays.stream(profileInVariables.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(ArrayList::new));
            if (profilesActive.isEmpty()) {
                profilesActive.add(DEFAULT_PROFILE);
            }
            return profilesActive;
        });
    }
}