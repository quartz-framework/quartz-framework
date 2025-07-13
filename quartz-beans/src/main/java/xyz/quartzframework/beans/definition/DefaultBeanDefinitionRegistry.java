package xyz.quartzframework.beans.definition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.config.BeanDefinition;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.exception.BeanCreationException;
import xyz.quartzframework.beans.support.exception.BeanNotFoundException;

import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@NoProxy
@Getter
@AllArgsConstructor
public class DefaultBeanDefinitionRegistry implements QuartzBeanDefinitionRegistry {

    private final URLClassLoader classLoader;

    private final Set<QuartzBeanDefinition> beanDefinitions = new HashSet<>();

    private static final Comparator<QuartzBeanDefinition> PREFERRED_COMPARATOR = Comparator
            .comparingInt((QuartzBeanDefinition def) -> {
                if (def.isPreferred()) return 0;
                if (!def.isSecondary()) return 1;
                return 2;
            });

    @NonNull
    @Override
    public QuartzBeanDefinition getBeanDefinition(@NonNull String beanName) {
        val matches = getBeanDefinitions()
                .stream()
                .filter(b -> b.getName().equals(beanName))
                .toList();
        if (matches.isEmpty()) {
            throw new BeanNotFoundException("No beans found for " + beanName);
        }
        return resolveUniqueDefinition(matches);
    }

    @Override
    public boolean containsBeanDefinition(@NonNull String beanName) {
        return getBeanDefinitions()
                .stream()
                .anyMatch(b -> b.getName().equals(beanName));
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanDefinitions()
                .stream()
                .map(QuartzBeanDefinition::getName)
                .toArray(String[]::new);
    }

    @Override
    public int getBeanDefinitionCount() {
        return getBeanDefinitions().size();
    }

    @Override
    public boolean isBeanNameInUse(@NonNull String beanName) {
        return containsBeanDefinition(beanName);
    }

    @Override
    public QuartzBeanDefinition getBeanDefinition(Class<?> requiredType) {
        return getBeanDefinition(TypeMetadata.raw(requiredType, classLoader));
    }

    @Override
    public QuartzBeanDefinition getBeanDefinition(String beanName, Class<?> requiredType) {
        return getBeanDefinition(beanName, TypeMetadata.raw(requiredType, classLoader));
    }

    @Override
    public QuartzBeanDefinition getBeanDefinition(TypeMetadata metadata) {
        return getBeanDefinitions()
                .stream()
                .filter(b -> b.getTypeMetadata().matches(metadata))
                .findFirst()
                .orElseThrow(() -> new BeanNotFoundException("No beans found for " + metadata.getFullName()));
    }

    @Override
    public QuartzBeanDefinition getBeanDefinition(String beanName, TypeMetadata metadata) {
        return getBeanDefinitions()
                .stream()
                .filter(b -> b.getName().equals(beanName))
                .filter(b -> b.getTypeMetadata().matches(metadata))
                .findFirst()
                .orElseGet(() -> getBeanDefinition(metadata));
    }

    @Override
    public boolean containsBeanDefinition(String beanName, Class<?> requiredType) {
        return containsBeanDefinition(beanName, TypeMetadata.raw(requiredType, classLoader));
    }

    @Override
    public boolean containsBeanDefinition(String beanName, TypeMetadata metadata) {
        return getBeanDefinitions()
                .stream()
                .filter(filterBeanDefinition(metadata))
                .anyMatch(b -> b.getName().equals(beanName));
    }

    @Override
    public boolean containsBeanDefinition(TypeMetadata metadata) {
        return getBeanDefinitions()
                .stream()
                .anyMatch(filterBeanDefinition(metadata));
    }

    @Override
    public void unregisterBeanDefinition(UUID id) {
        getBeanDefinitions().removeIf(bean -> {
            if (bean.getId().equals(id)) {
                bean.destroy();
                return true;
            }
            return false;
        });
    }

    @Override
    public void removeBeanDefinition(@NonNull String beanName) {
        unregisterBeanDefinition(getBeanDefinition(beanName).getId());
    }

    @Override
    public void registerBeanDefinition(@NonNull String beanName, @NonNull BeanDefinition beanDefinition) {
        if (!(beanDefinition instanceof QuartzBeanDefinition quartzBeanDefinition)) {
            throw new BeanCreationException("Unrecognized bean definition type: " + beanDefinition.getClass().getName());
        }
        if (quartzBeanDefinition.isPreferred() && quartzBeanDefinition.isSecondary()) {
            log.warn("Bean '{}' is annotated as both @Preferred and @Secondary — ignoring @Secondary.", quartzBeanDefinition.getName());
            quartzBeanDefinition.setSecondary(false);
        }
        beanDefinitions.removeIf(d -> d.getName().equals(beanName));
        quartzBeanDefinition.setName(beanName);
        getBeanDefinitions().add(quartzBeanDefinition);
    }

    @Override
    public <T> void updateBeanInstance(QuartzBeanDefinition definition, T instance) {
        definition.setInstance(instance);
        definition.setInjected(true);
        if (!definition.isClassLoaded()) {
            definition.getTypeMetadata().getType();
            definition.setClassLoaded(true);
        }
    }

    @Override
    public Predicate<QuartzBeanDefinition> filterBeanDefinition(Class<?> requiredType) {
        return filterBeanDefinition(TypeMetadata.raw(requiredType, classLoader));
    }

    @Override
    public Set<QuartzBeanDefinition> getBeanDefinitionsByType(Class<?> requiredType) {
        return getBeanDefinitions()
                .stream()
                .filter(b -> {
                    try {
                        return b.getTypeMetadata().isAssignableTo(TypeMetadata.raw(requiredType, classLoader));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted(PREFERRED_COMPARATOR)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<QuartzBeanDefinition> getBeanDefinitionsByType(TypeMetadata metadata) {
        return getBeanDefinitions()
                .stream()
                .filter(b -> b.getTypeMetadata().matches(metadata))
                .sorted(PREFERRED_COMPARATOR)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Predicate<QuartzBeanDefinition> filterBeanDefinition(TypeMetadata metadata) {
        return beanDefinition -> metadata.matches(beanDefinition.getTypeMetadata());
    }

    private QuartzBeanDefinition resolveUniqueDefinition(List<QuartzBeanDefinition> candidates) {
        if (candidates.size() <= 1) return candidates.get(0);
        val preferred = candidates.stream().filter(QuartzBeanDefinition::isPreferred).toList();
        if (preferred.size() > 1) {
            log.warn("Multiple @Preferred beans found: {}", preferred.stream().map(QuartzBeanDefinition::getName).toList());
        }
        if (!preferred.isEmpty()) return preferred.get(0);
        val normal = candidates.stream()
                .filter(d -> !d.isPreferred() && !d.isSecondary())
                .toList();
        if (!normal.isEmpty()) return normal.get(0);
        val secondary = candidates.stream().filter(QuartzBeanDefinition::isSecondary).toList();
        if (secondary.size() > 1) {
            log.warn("Multiple @Secondary beans found: {}", secondary.stream().map(QuartzBeanDefinition::getName).toList());
        }
        return secondary.get(0);
    }
}