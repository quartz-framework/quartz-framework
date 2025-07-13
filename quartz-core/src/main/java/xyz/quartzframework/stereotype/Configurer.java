package xyz.quartzframework.stereotype;

import xyz.quartzframework.Injectable;

import java.lang.annotation.*;

@Injectable
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configurer {

    boolean force() default false;

}