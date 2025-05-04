package xyz.quartzframework.core.context.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface External {

    Class<?>[] value();

}
