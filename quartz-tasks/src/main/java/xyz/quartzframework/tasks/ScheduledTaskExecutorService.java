package xyz.quartzframework.tasks;

import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public interface ScheduledTaskExecutorService extends TaskExecutorService {

    default TaskHandle schedule(Runnable task, long delay) {
        return schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    TaskHandle schedule(Runnable task, long delay, TimeUnit unit);

    TaskHandle scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

    TaskHandle scheduleWithTimeout(Runnable task, long delay, TimeUnit unit, long timeout, TimeUnit timeoutUnit);

    TaskHandle scheduleCron(Runnable task, String cronExpression, ZoneId zoneId);

}