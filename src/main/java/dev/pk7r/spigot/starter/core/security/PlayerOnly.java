package dev.pk7r.spigot.starter.core.security;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to only allow calls from players (console not allowed)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PluginAuthorize("#root instanceof T(org.bukkit.entity.Player)")
public @interface PlayerOnly {

    /**
     * The message to be thrown in {@link dev.pk7r.spigot.starter.core.exception.PermissionDeniedException PermissionDeniedException}
     * if the sender is not a player.
     */
    @AliasFor(annotation = PluginAuthorize.class, attribute = "message")
    String message() default "";

}