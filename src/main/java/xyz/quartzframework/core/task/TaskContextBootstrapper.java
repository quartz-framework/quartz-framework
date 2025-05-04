package xyz.quartzframework.core.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;
import xyz.quartzframework.core.property.Property;

@Slf4j
@NoProxy
@ContextBootstrapper
@RequiredArgsConstructor
public class TaskContextBootstrapper {

    @Provide
    @ActivateWhenBeanMissing(ScheduledTaskExecutorService.class)
    ScheduledTaskExecutorService scheduledTaskExecutorService(@Property("${quartz.default-task-pool.size:5}") int poolSize) {
        return new DefaultScheduledTaskExecutorService(poolSize);
    }

    @Provide
    @ActivateWhenBeanMissing(TaskFactory.class)
    TaskFactory taskFactory(ScheduledTaskExecutorService scheduledTaskExecutorService) {
        val factory = new DefaultTaskFactory();
        factory.register("default", scheduledTaskExecutorService);
        log.info("Initializing default task factory");
        return factory;
    }
}