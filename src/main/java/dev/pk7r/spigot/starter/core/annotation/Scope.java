package dev.pk7r.spigot.starter.core.annotation;

import dev.pk7r.spigot.starter.core.bean.BeanScope;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

    BeanScope value() default BeanScope.SINGLETON;

}