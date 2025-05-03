package xyz.quartzframework.core.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.ContextStarts;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;
import xyz.quartzframework.core.condition.annotation.ActivateWhenAnnotationPresent;
import xyz.quartzframework.core.context.QuartzContext;
import xyz.quartzframework.core.util.InjectionUtil;

import javax.annotation.PreDestroy;
import java.time.ZoneId;

@Slf4j
@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
@ActivateWhenAnnotationPresent(EnableRepeatedTasks.class)
public class TaskInitializationContextBootstrapper {

    private final PluginBeanFactory pluginBeanFactory;

    private final TaskFactory taskFactory;

    @PreDestroy
    public void onDestroy() {
        taskFactory.shutdownAll();
    }

    @ContextStarts
    public void onStart() {
        val context = pluginBeanFactory.getBean(QuartzContext.class);
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