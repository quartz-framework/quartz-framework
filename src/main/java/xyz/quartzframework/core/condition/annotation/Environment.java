package xyz.quartzframework.core.condition.annotation;

import xyz.quartzframework.core.condition.Evaluators;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Environment {

    String[] value() default Evaluators.DEFAULT_PROFILE;

}