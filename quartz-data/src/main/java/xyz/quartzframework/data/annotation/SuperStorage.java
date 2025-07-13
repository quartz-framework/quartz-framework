package xyz.quartzframework.data.annotation;

import org.aopalliance.intercept.MethodInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuperStorage {

    Class<?> value();

    Class<? extends MethodInterceptor>[] interceptors() default {};

}