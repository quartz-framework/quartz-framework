package dev.pk7r.spigot.starter.core.task;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Future;

@RequiredArgsConstructor
public class TaskHandle {

    private final Future<?> future;

    public boolean cancel() {
        return cancel(true);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    public boolean isDone() {
        return future.isDone();
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

}