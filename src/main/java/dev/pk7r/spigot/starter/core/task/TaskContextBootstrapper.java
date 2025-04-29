package dev.pk7r.spigot.starter.core.task;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Property;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenBeanMissing;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@NoProxy
@ContextBootstrapper
public class TaskContextBootstrapper {

    @Provide
    @ActivateWhenBeanMissing(ScheduledTaskExecutorService.class)
    ScheduledTaskExecutorService scheduledTaskExecutorService(@Property("${spigot.default-task-pool.size:5}") int poolSize) {
        return new DefaultScheduledTaskExecutorService(poolSize);
    }

    @Provide
    @ActivateWhenBeanMissing(TaskFactory.class)
    TaskFactory taskFactory(ScheduledTaskExecutorService scheduledTaskExecutorService) {
        val factory = new DefaultTaskFactory();
        factory.register("default", scheduledTaskExecutorService);
        return factory;
    }
}
