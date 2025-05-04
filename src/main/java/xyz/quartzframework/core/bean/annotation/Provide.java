package xyz.quartzframework.core.bean.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

@Autowired
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Provide {

    String[] postConstructMethods() default {};

    String[] preDestroyMethods() default {};

}