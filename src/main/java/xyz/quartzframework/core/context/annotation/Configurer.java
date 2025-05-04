package xyz.quartzframework.core.context.annotation;

import xyz.quartzframework.core.bean.annotation.Injectable;

import java.lang.annotation.*;

@Injectable
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configurer {

    boolean force() default false;

}