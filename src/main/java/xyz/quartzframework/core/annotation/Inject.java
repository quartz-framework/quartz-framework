package xyz.quartzframework.core.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

@Autowired
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Inject {

}