package xyz.quartzframework.beans.support.annotation.condition;

import xyz.quartzframework.beans.condition.GenericCondition;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivateWhen {

    Class<? extends GenericCondition> value();

}