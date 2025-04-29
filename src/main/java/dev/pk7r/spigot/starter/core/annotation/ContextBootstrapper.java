package dev.pk7r.spigot.starter.core.annotation;

import org.springframework.core.annotation.AliasFor;

import javax.annotation.ManagedBean;
import java.lang.annotation.*;

@Injectable
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextBootstrapper {

}