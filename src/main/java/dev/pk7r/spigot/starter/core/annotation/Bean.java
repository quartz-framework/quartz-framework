package dev.pk7r.spigot.starter.core.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

    String[] postConstructMethods() default {};

    String[] preDestroyMethods() default {};

}