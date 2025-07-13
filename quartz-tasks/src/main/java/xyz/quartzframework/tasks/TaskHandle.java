package xyz.quartzframework.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Future;

@RequiredArgsConstructor
public class TaskHandle {

    @Getter
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