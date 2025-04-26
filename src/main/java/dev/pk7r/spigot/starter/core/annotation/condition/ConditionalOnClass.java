package dev.pk7r.spigot.starter.core.annotation.condition;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalOnClass {

    Class<?>[] value() default {};

    String[] classNames() default {};

}