package xyz.quartzframework.core.security;

import org.springframework.core.annotation.AliasFor;
import xyz.quartzframework.core.exception.PermissionDeniedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to only allow calls from sender with the defined permission(s)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PluginAuthorize("#params.?[!#root.hasPermission(#this)].length == 0")
public @interface HasPermission {

    /**
     * The permission array to be checked over the current sender in the {@link xyz.quartzframework.core.session.PlayerSession}.
     * All permissions must be satisfied to the call be allowed.
     */
    @AliasFor(annotation = PluginAuthorize.class, attribute = "params")
    String[] value();

    /**
     * The message to be thrown in {@link PermissionDeniedException PermissionDeniedException}
     * if the sender is not a player.
     */
    @AliasFor(annotation = PluginAuthorize.class, attribute = "message")
    String message() default "";

}