package xyz.quartzframework.core.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Property {

    @AliasFor(annotation = Value.class, attribute = "value")
    String value();

    String source() default "application";

}