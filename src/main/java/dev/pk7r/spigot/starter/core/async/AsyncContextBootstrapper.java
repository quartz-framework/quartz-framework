package dev.pk7r.spigot.starter.core.async;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.Inject;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.task.ScheduledTaskExecutorService;
import dev.pk7r.spigot.starter.core.task.TaskFactory;
import lombok.RequiredArgsConstructor;

@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
public class AsyncContextBootstrapper {

    @Inject
    public AsyncContextBootstrapper(TaskFactory taskFactory, ScheduledTaskExecutorService scheduledTaskExecutorService) {
        taskFactory.register("default-async-pool", scheduledTaskExecutorService);
    }
}