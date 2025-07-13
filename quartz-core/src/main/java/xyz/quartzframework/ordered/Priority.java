package xyz.quartzframework.ordered;

import org.springframework.core.Ordered;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface Priority {

    int value() default Ordered.HIGHEST_PRECEDENCE;

}