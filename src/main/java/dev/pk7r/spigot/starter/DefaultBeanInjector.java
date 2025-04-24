package dev.pk7r.spigot.starter;

import dev.pk7r.spigot.starter.annotation.Inject;
import dev.pk7r.spigot.starter.exception.BeanNotFoundException;
import dev.pk7r.spigot.starter.factory.bean.BeanFactory;
import dev.pk7r.spigot.starter.injector.BeanInjector;
import dev.pk7r.spigot.starter.util.BeanUtil;
import dev.pk7r.spigot.starter.util.ReflectionUtil;
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
                field.set(BeanUtil.newInstance(getBeanFactory(), clazz), instance);
            } else field.set(getBeanFactory().getBean(clazz), instance);
            inject(instance.getClass());
        }
    }
}