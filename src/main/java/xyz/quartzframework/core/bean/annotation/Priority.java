package xyz.quartzframework.core.bean.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface Priority {

    int value() default Integer.MAX_VALUE;

}