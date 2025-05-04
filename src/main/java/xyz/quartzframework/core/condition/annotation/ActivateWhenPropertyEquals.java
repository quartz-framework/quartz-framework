package xyz.quartzframework.core.condition.annotation;

import xyz.quartzframework.core.property.Property;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivateWhenPropertyEquals {

    Property value();

    String expected();

}