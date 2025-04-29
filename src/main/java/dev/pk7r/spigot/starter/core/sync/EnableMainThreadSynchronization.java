package dev.pk7r.spigot.starter.core.sync;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface EnableMainThreadSynchronization {

}