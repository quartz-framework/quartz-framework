package xyz.quartzframework.core.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.PluginManager;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.bean.registry.PluginBeanDefinitionRegistry;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;
import xyz.quartzframework.core.task.TaskFactory;

@NoProxy
@Slf4j
@RequiredArgsConstructor
@ContextBootstrapper
public class EventPublishContextBootstrapper {

    private final TaskFactory taskFactory;

    private final PluginBeanDefinitionRegistry registry;

    @Provide
    @ActivateWhenBeanMissing(EventPublisher.class)
    EventPublisher eventPublisher(PluginManager pluginManager) {
        return new DefaultEventPublisher(registry, pluginManager, taskFactory);
    }
}