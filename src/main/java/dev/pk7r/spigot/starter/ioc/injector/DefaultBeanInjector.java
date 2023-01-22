package dev.pk7r.spigot.starter.ioc.injector;

import dev.pk7r.spigot.starter.ioc.annotation.Inject;
import dev.pk7r.spigot.starter.ioc.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.ioc.factory.bean.BeanFactory;
import dev.pk7r.spigot.starter.ioc.util.BeanUtil;
import dev.pk7r.spigot.starter.ioc.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

@AllArgsConstructor
public class DefaultBeanInjector implements BeanInjector {

    @Getter
    private BeanFactory beanFactory;

    @Override
    @SneakyThrows
    public void inject(Class<?> clazz) {
        for (val field : ReflectionUtil.getFields(clazz, Inject.class)) {
            field.setAccessible(true);
            Object instance;
            if (BeanUtil.hasNamedInstance(field)) {
                if (!getBeanFactory().containsBean(BeanUtil.getNamedInstance(field), field.getType())) {
                    throw new BeanNotFoundException(String.format("No beans %s found for %s",
                            BeanUtil.getNamedInstance(field),
                            field.getType().getSimpleName()));
                }
                instance = getBeanFactory().getBean(BeanUtil.getNamedInstance(field), field.getType());
            } else instance = getBeanFactory().getBean(field.getType());
            if (BeanUtil.isBean(clazz)) {
                field.set(clazz.newInstance(), instance);
            } else field.set(getBeanFactory().getBean(clazz), instance);
            inject(instance.getClass());
        }
    }
}