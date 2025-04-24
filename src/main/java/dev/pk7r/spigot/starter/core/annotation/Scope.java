package dev.pk7r.spigot.starter.core.annotation;

import dev.pk7r.spigot.starter.core.model.BeanScope;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

    BeanScope value() default BeanScope.SINGLETON;

}