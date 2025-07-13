package xyz.quartzframework.tasks;

import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

class DefaultTaskFactory implements TaskFactory {

    private final Map<String, ScheduledTaskExecutorService> executors = new ConcurrentHashMap<>();

    @Override
    public void register(String name, ScheduledTaskExecutorService executorService) {
        if (executors.containsKey(name)) {
            throw new IllegalArgumentException("Executor already registered with name: " + name);
        }
        executors.put(name, executorService);
    }

    @Override
    public ScheduledTaskExecutorService getExecutor(String name) {
        ScheduledTaskExecutorService executor = executors.get(name);
        if (executor == null) {
            throw new IllegalArgumentException("No executor registered with name: " + name);
        }
        return executor;
    }

    @Override
    public TaskHandle submit(String executorName, Runnable task) {
        return getExecutor(executorName).submit(task);
    }

    @Override
    public <T> TaskHandle submit(String executorName, Callable<T> task) {
        return getExecutor(executorName).submit(task);
    }

    @Override
    public TaskHandle submitWithTimeout(String executorName, Runnable task, long timeout, TimeUnit unit) {
        return getExecutor(executorName).submitWithTimeout(task, timeout, unit);
    }

    @Override
    public <T> TaskHandle submitWithTimeout(String executorName, Callable<T> task, long timeout, TimeUnit unit) {
        return getExecutor(executorName).submitWithTimeout(task, timeout, unit);
    }

    @Override
    public TaskHandle schedule(String executorName, Runnable task, long delay, TimeUnit unit) {
        return getExecutor(executorName).schedule(task, delay, unit);
    }

    @Override
    public TaskHandle scheduleAtFixedRate(String executorName, Runnable task, long initialDelay, long period, TimeUnit unit) {
        return getExecutor(executorName).scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    @Override
    public TaskHandle scheduleCron(String executorName, Runnable task, String cronExpression, ZoneId zoneId) {
        return getExecutor(executorName).scheduleCron(task, cronExpression, zoneId);
    }

    @Override
    public void shutdownAll() {
        for (ScheduledTaskExecutorService executor : executors.values()) {
            executor.shutdown();
        }
        executors.clear();
    }
}