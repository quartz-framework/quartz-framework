package dev.pk7r.spigot.starter.core.security;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to only allow calls from op senders
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PluginAuthorize("isOp()")
public @interface OpOnly {

    /**
     * The message to be thrown in {@link dev.pk7r.spigot.starter.core.exception.PermissionDeniedException PermissionDeniedException}
     * if the sender is not a operator.
     */
    @AliasFor(annotation = PluginAuthorize.class, attribute = "message")
    String message() default "";

}