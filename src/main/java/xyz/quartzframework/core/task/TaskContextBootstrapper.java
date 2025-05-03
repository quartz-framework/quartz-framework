package xyz.quartzframework.core.task;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Property;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;

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