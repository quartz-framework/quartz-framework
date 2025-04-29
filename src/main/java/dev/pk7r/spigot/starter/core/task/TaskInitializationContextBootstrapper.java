package dev.pk7r.spigot.starter.core.task;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenAnnotationPresent;
import dev.pk7r.spigot.starter.core.context.AbstractPluginContext;
import dev.pk7r.spigot.starter.core.event.ContextStartedEvent;
import dev.pk7r.spigot.starter.core.util.InjectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.PreDestroy;
import java.time.ZoneId;

@Slf4j
@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
@ActivateWhenAnnotationPresent(EnableRepeatedTasks.class)
public class TaskInitializationContextBootstrapper implements Listener {

    private final AbstractPluginContext pluginContext;

    private final TaskFactory taskFactory;

    @PreDestroy
    public void onDestroy() {
        taskFactory.shutdownAll();
    }

    @EventHandler
    public void onStart(ContextStartedEvent event) {
        val context = event.getContext();
        if (!context.getId().equals(pluginContext.getId())) {
            return;
        }
        val beanDefinitionRegistry = context.getBeanDefinitionRegistry();
        val beanFactory = context.getBeanFactory();
        beanDefinitionRegistry
                .getBeanDefinitions()
                .stream()
                .flatMap(definition -> definition.getRepeatedTasksMethods().stream())
                .forEach(taskMethod -> {
                    val annotation = taskMethod.getAnnotation(RepeatedTask.class);
                    if (annotation == null) return;
                    val executorName = annotation.executorName();
                    val initialDelay = annotation.initialDelay();
                    val fixedDelay = annotation.fixedDelay();
                    val timeUnit = annotation.timeUnit();
                    val cron = annotation.cron();
                    val zoneId = annotation.zoneId();
                    Runnable task = () -> InjectionUtil.newInstance(beanFactory, taskMethod);
                    if (fixedDelay == -1) {
                        taskFactory.scheduleCron(executorName, task, cron, zoneId.equalsIgnoreCase("default") ? ZoneId.systemDefault() : ZoneId.of(zoneId));
                    } else {
                        taskFactory.scheduleAtFixedRate(executorName, task, initialDelay, fixedDelay, timeUnit);
                    }
                });
        val count = beanDefinitionRegistry
                .getBeanDefinitions()
                .stream()
                .mapToLong(definition -> definition.getRepeatedTasksMethods().size())
                .sum();
        log.info("Initialized {} repeated tasks", count);
    }
}