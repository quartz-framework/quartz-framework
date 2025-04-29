package dev.pk7r.spigot.starter.core.annotation;

import java.lang.annotation.*;

@Injectable
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bootstrapper {

}