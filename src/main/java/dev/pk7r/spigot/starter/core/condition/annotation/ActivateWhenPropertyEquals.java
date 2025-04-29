package dev.pk7r.spigot.starter.core.condition.annotation;

import dev.pk7r.spigot.starter.core.annotation.Property;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivateWhenPropertyEquals {

    Property value();

    String expected();

}