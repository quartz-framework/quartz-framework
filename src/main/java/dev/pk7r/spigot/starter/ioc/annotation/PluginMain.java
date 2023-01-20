package dev.pk7r.spigot.starter.ioc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginMain {

    String[] basePackages();

    String[] exclude() default "";

    boolean verbose() default false;

}