package xyz.quartzframework.core.annotation;

import javax.annotation.ManagedBean;
import java.lang.annotation.*;

@ManagedBean
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Injectable {

}