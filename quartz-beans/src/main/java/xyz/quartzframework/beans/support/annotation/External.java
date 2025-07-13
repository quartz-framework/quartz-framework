package xyz.quartzframework.beans.support.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface External {

    Class<?>[] value();

}
