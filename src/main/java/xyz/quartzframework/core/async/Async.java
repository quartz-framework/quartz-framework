package xyz.quartzframework.core.async;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {

    String executorName() default "default-async-pool";

}