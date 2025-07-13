package xyz.quartzframework.tasks;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.*;

public class DefaultScheduledTaskExecutorService implements ScheduledTaskExecutorService {

    private final CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));

    private final ScheduledExecutorService scheduledExecutorService;

    private final ScheduledExecutorService timeoutScheduler = Executors.newScheduledThreadPool(1);

    public DefaultScheduledTaskExecutorService(int poolSize) {

        this(poolSize, Executors.defaultThreadFactory());
    }

    public DefaultScheduledTaskExecutorService(int poolSize, ThreadFactory threadFactory) {
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(poolSize, threadFactory);
    }

    @Override
    public TaskHandle submit(Runnable task) {
        Future<?> future = scheduledExecutorService.submit(task);
        return new TaskHandle(future);
    }

    @Override
    public <T> TaskHandle submit(Callable<T> task) {
        Future<T> future = scheduledExecutorService.submit(task);
        return new TaskHandle(future);
    }

    @Override
    public TaskHandle submitWithTimeout(Runnable task, long timeout, TimeUnit unit) {
        Future<?> future = scheduledExecutorService.submit(task);
        timeoutScheduler.schedule(() -> future.cancel(true), timeout, unit);
        return new TaskHandle(future);
    }

    @Override
    public <T> TaskHandle submitWithTimeout(Callable<T> task, long timeout, TimeUnit unit) {
        Future<T> future = scheduledExecutorService.submit(task);
        timeoutScheduler.schedule(() -> future.cancel(true), timeout, unit);
        return new TaskHandle(future);
    }

    @Override
    public TaskHandle schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = scheduledExecutorService.schedule(task, delay, unit);
        return new TaskHandle(future);
    }

    @Override
    public TaskHandle scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(task, initialDelay, period, unit);
        return new TaskHandle(future);
    }

    @Override
    public TaskHandle scheduleWithTimeout(Runnable task, long delay, TimeUnit delayUnit, long timeout, TimeUnit timeoutUnit) {
        ScheduledFuture<?> future = scheduledExecutorService.schedule(task, delay, delayUnit);
        timeoutScheduler.schedule(() -> future.cancel(true), timeout, timeoutUnit);
        return new TaskHandle(future);
    }

    @Override
    public TaskHandle scheduleCron(Runnable task, String cronExpression, ZoneId zoneId) {
        Cron cron = cronParser.parse(cronExpression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        return scheduleNextCron(task, executionTime, zoneId);
    }

    private TaskHandle scheduleNextCron(Runnable task, ExecutionTime executionTime, ZoneId zoneId) {
        Optional<Duration> nextExecution = executionTime.timeToNextExecution(ZonedDateTime.now(zoneId));
        if (nextExecution.isEmpty()) {
            return null;
        }
        long delayMillis = nextExecution.get().toMillis();
        ScheduledFuture<?> future = scheduledExecutorService.schedule(() -> {
            try {
                task.run();
            } finally {
                scheduleNextCron(task, executionTime, zoneId);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
        return new TaskHandle(future);
    }

    @Override
    public void shutdown() {
        scheduledExecutorService.shutdown();
        timeoutScheduler.shutdown();
    }
}