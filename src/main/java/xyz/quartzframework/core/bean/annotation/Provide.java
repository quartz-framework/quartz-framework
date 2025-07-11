package xyz.quartzframework.core.bean.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Provide {

    String initMethodName() default "<none>";

    String destroyMethodName() default "<none>";

}