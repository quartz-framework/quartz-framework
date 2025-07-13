package xyz.quartzframework.beans.support.annotation.condition;

import xyz.quartzframework.beans.condition.Evaluators;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Environment {

    String[] value() default Evaluators.DEFAULT_PROFILE;

}