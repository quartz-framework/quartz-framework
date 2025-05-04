package xyz.quartzframework.core.async;

import lombok.RequiredArgsConstructor;
import xyz.quartzframework.core.bean.annotation.Inject;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;
import xyz.quartzframework.core.task.ScheduledTaskExecutorService;
import xyz.quartzframework.core.task.TaskFactory;

@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
public class AsyncContextBootstrapper {

    @Inject
    public AsyncContextBootstrapper(TaskFactory taskFactory, ScheduledTaskExecutorService scheduledTaskExecutorService) {
        taskFactory.register("default-async-pool", scheduledTaskExecutorService);
    }
}