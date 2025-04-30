package xyz.quartzframework.core.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Server;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenAnnotationPresent;
import xyz.quartzframework.core.scheduler.Scheduler;

@Slf4j
@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
public class SynchronizeAspectContextBootstrapper {

    private final Scheduler scheduler;

    private final Server server;

    @Provide
    @ActivateWhenAnnotationPresent(EnableMainThreadSynchronization.class)
    SynchronizeAspect synchronizeAspect() {
        log.info("Enabling @Synchronize feature");
        return new SynchronizeAspect(scheduler, server);
    }
}