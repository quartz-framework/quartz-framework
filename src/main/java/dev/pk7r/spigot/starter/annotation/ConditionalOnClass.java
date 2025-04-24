package dev.pk7r.spigot.starter.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalOnClass {

    String[] classNames();

}