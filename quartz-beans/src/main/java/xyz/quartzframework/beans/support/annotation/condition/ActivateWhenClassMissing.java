package xyz.quartzframework.beans.support.annotation.condition;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivateWhenClassMissing {

    Class<?>[] value() default {};

    String[] classNames() default {};

}