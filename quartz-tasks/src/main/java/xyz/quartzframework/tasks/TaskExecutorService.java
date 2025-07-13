package xyz.quartzframework.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

interface TaskExecutorService {

    TaskHandle submit(Runnable task);

    <T> TaskHandle submit(Callable<T> task);

    TaskHandle submitWithTimeout(Runnable task, long timeout, TimeUnit unit);

    <T> TaskHandle submitWithTimeout(Callable<T> task, long timeout, TimeUnit unit);

    void shutdown();

}