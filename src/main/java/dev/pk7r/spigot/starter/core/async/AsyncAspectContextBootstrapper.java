package dev.pk7r.spigot.starter.core.async;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenAnnotationPresent;
import dev.pk7r.spigot.starter.core.task.TaskFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoProxy
@Slf4j
@RequiredArgsConstructor
@ContextBootstrapper
public class AsyncAspectContextBootstrapper {

    @Provide
    @ActivateWhenAnnotationPresent(EnableAsyncMethods.class)
    AsyncAspect asyncAspect(TaskFactory taskFactory) {
        log.info("Enabling @Async feature...");
        return new AsyncAspect(taskFactory);
    }
}