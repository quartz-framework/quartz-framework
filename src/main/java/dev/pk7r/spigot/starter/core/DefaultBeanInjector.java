package dev.pk7r.spigot.starter.core;

import dev.pk7r.spigot.starter.core.annotation.Inject;
import dev.pk7r.spigot.starter.core.annotation.Value;
import dev.pk7r.spigot.starter.core.property.PropertyPostProcessor;
import dev.pk7r.spigot.starter.core.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.core.factory.BeanFactory;
import dev.pk7r.spigot.starter.core.injector.BeanInjector;
import dev.pk7r.spigot.starter.core.util.BeanUtil;
import dev.pk7r.spigot.starter.core.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

@AllArgsConstructor
class DefaultBeanInjector implements BeanInjector {

    @Getter
    private BeanFactory beanFactory;

    @Override
    @SneakyThrows
    public void inject(Class<?> clazz) {
        for (val field : ReflectionUtil.getFields(clazz, Inject.class, Value.class)) {
            field.setAccessible(true);
            Object instance;
            val type = field.getType();
            val value = field.getAnnotation(Value.class);
            if (value != null) {
                val environmentPostProcessor = beanFactory.getBean(PropertyPostProcessor.class);
                instance = environmentPostProcessor.process(value.value(), value.source(), type);
            } else {
                if (BeanUtil.hasNamedInstance(field)) {
                    if (!getBeanFactory().containsBean(BeanUtil.getNamedInstance(field), type)) {
                        throw new BeanNotFoundException(String.format("No beans %s found for %s",
                                BeanUtil.getNamedInstance(field),
                                type.getSimpleName()));
                    }
                    instance = getBeanFactory().getBean(BeanUtil.getNamedInstance(field), type);
                } else instance = getBeanFactory().getBean(type);
            }
            field.set(getBeanFactory().getBean(clazz), instance);
            inject(instance.getClass());
        }
    }
}