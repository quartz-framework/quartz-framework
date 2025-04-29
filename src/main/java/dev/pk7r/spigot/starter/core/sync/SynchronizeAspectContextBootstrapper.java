package dev.pk7r.spigot.starter.core.sync;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenAnnotationPresent;
import dev.pk7r.spigot.starter.core.task.TaskFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Server;

@Slf4j
@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
public class SynchronizeAspectContextBootstrapper {

    private final TaskFactory taskFactory;

    private final Server server;

    @Provide
    @ActivateWhenAnnotationPresent(EnableMainThreadSynchronization.class)
    SynchronizeAspect synchronizeAspect() {
        log.info("Enabling @Synchronize feature");
        return new SynchronizeAspect(taskFactory, server);
    }
}