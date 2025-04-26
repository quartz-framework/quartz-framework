package dev.pk7r.spigot.starter.core.listener;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.Listener;

import javax.annotation.PostConstruct;

@Slf4j
@RequiredArgsConstructor
@AutoConfiguration(force = true)
public class EventAutoConfiguration {

    private final BeanFactory beanFactory;

    private final ListenerFactory listenerFactory;

    @PostConstruct
    public void postConstruct() {
        val events = beanFactory.getBeansOfType(Listener.class);
        events.forEach(listenerFactory::registerEvents);
        log.info("Registered {} listeners", events.size());
    }
}