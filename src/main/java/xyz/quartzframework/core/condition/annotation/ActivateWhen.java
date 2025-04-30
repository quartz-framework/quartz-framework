package xyz.quartzframework.core.condition.annotation;

import xyz.quartzframework.core.condition.GenericCondition;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivateWhen {

    Class<? extends GenericCondition> value();

}