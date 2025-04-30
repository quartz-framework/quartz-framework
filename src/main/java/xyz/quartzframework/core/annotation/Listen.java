package xyz.quartzframework.core.annotation;

import org.bukkit.event.EventPriority;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

@Autowired
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Listen {

    EventPriority priority() default EventPriority.NORMAL;

    boolean ignoreCancelled() default false;

}