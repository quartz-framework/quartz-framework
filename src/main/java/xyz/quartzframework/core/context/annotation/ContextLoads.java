package xyz.quartzframework.core.context.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

@Autowired
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextLoads {

}