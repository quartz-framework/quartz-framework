package xyz.quartzframework.core.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenAnnotationPresent;
import xyz.quartzframework.core.task.TaskFactory;

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