package dev.pk7r.spigot.starter.core.listener;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;
import dev.pk7r.spigot.starter.core.util.InjectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.Listener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
public class EventContextBootstrapper {

    private final PluginBeanFactory pluginBeanFactory;

    private final ListenerFactory listenerFactory;

    private final List<Listener> dynamicListeners = new ArrayList<>();

    @PreDestroy
    public void onDestroy() {
        dynamicListeners.forEach(listenerFactory::unregisterEvents);
        dynamicListeners.clear();
    }

    @PostConstruct
    public void postConstruct() {
        val events = pluginBeanFactory.getBeansOfType(Listener.class).values();
        events.forEach(listener -> listenerFactory.registerEvents(InjectionUtil.unwrapIfProxy(listener)));
        val registry = pluginBeanFactory.getRegistry();
        registry
                .getBeanDefinitions()
                .stream()
                .filter(def -> !def.getListenMethods().isEmpty())
                .forEach(def -> {
                    val bean = pluginBeanFactory.getBean(def.getName(), def.getType());
                    val realBean = InjectionUtil.unwrapIfProxy(bean);
                    listenerFactory.registerEvents(realBean);
                    if (!(bean instanceof Listener)) {
                        dynamicListeners.add(new Listener() {});
                    }
                });
        long count = registry
                .getBeanDefinitions()
                .stream()
                .mapToLong(def -> def.getListenMethods().size())
                .sum();
        log.info("Registered {} listeners", events.size() + count);
    }
}