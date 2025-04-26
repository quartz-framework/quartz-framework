package dev.pk7r.spigot.starter.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER })
public @interface NamedInstance {

    String value();

}