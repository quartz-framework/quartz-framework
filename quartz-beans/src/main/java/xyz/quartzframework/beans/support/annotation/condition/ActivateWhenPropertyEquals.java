package xyz.quartzframework.beans.support.annotation.condition;

import xyz.quartzframework.config.Property;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivateWhenPropertyEquals {

    String expression();

    String expected();

    String source() default "application";

}