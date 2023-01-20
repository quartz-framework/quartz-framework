package dev.pk7r.spigot.starter.ioc.annotation;

import dev.pk7r.spigot.starter.ioc.model.BeanScope;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

    BeanScope value() default BeanScope.SINGLETON;

}