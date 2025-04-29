package dev.pk7r.spigot.starter.core.condition.annotation;

import dev.pk7r.spigot.starter.core.condition.GenericCondition;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivateWhen {

    Class<? extends GenericCondition> value();

}