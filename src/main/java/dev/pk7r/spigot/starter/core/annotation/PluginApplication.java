package dev.pk7r.spigot.starter.core.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginApplication {

    String[] basePackages();

    String[] exclude() default "";

    boolean verbose() default false;

}