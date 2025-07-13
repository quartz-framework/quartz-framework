package xyz.quartzframework.context;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuartzApplication {

    String[] basePackages() default {};

    Class<?>[] excludeClasses() default {};

    String[] exclude() default "";

    boolean verbose() default false;

    boolean enableConfigurers() default true;

}