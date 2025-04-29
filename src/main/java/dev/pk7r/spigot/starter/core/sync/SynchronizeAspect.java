package dev.pk7r.spigot.starter.core.sync;

import dev.pk7r.spigot.starter.core.task.TaskFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bukkit.Server;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class SynchronizeAspect {

    private final TaskFactory taskFactory;

    private final Server server;

    @Around("within(@(@dev.pk7r.spigot.starter.core.sync.Synchronize *) *) " +
            "|| execution(@(@dev.pk7r.spigot.starter.core.sync.Synchronize *) * *(..)) " +
            "|| @within(dev.pk7r.spigot.starter.core.sync.Synchronize)" +
            "|| execution(@dev.pk7r.spigot.starter.core.sync.Synchronize * *(..))")
    public Object synchronizeCall(ProceedingJoinPoint joinPoint) throws Throwable {
        if (server.isPrimaryThread()) {
            return joinPoint.proceed();
        }
        taskFactory.submit("default", () -> {
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                log.error("Error in synchronous task", throwable);
            }
        });
        return null;
    }

}