package xyz.quartzframework.core.condition.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivateWhenAnnotationPresent {

    Class<? extends Annotation>[] value();

}