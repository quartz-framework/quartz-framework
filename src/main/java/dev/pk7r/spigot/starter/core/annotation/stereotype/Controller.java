package dev.pk7r.spigot.starter.core.annotation.stereotype;

import dev.pk7r.spigot.starter.core.annotation.Injectable;

import java.lang.annotation.*;

@Injectable
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

}