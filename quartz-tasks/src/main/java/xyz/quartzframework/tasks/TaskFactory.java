package xyz.quartzframework.tasks;

import java.time.ZoneId;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface TaskFactory {

    void register(String name, ScheduledTaskExecutorService executorService);

    ScheduledTaskExecutorService getExecutor(String name);

    TaskHandle submit(String executorName, Runnable task);

    <T> TaskHandle submit(String executorName, Callable<T> task);

    TaskHandle submitWithTimeout(String executorName, Runnable task, long timeout, TimeUnit unit);

    <T> TaskHandle submitWithTimeout(String executorName, Callable<T> task, long timeout, TimeUnit unit);

    TaskHandle schedule(String executorName, Runnable task, long delay, TimeUnit unit);

    TaskHandle scheduleAtFixedRate(String executorName, Runnable task, long initialDelay, long period, TimeUnit unit);

    TaskHandle scheduleCron(String executorName, Runnable task, String cronExpression, ZoneId zoneId);

    void shutdownAll();
}