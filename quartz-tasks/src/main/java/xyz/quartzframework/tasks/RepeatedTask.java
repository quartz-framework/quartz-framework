package xyz.quartzframework.tasks;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RepeatedTask {

    String cron() default "* * * * *";

    String zoneId() default "default";

    long fixedDelay() default -1L;

    long initialDelay() default 0;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    String executorName() default "default";

}