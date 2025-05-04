package xyz.quartzframework.core.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenAnnotationPresent;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;
import xyz.quartzframework.core.task.TaskFactory;

@Slf4j
@NoProxy
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