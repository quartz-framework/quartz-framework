package xyz.quartzframework;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuartzPlugin {

    String[] basePackages() default {};

    Class<?>[] excludeClasses() default {};

    String[] exclude() default "";

    boolean verbose() default false;

    boolean enableConfigurers() default true;

}