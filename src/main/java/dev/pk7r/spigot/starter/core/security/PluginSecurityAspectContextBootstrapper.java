package dev.pk7r.spigot.starter.core.security;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenAnnotationPresent;
import dev.pk7r.spigot.starter.core.session.PlayerSession;
import dev.pk7r.spigot.starter.core.session.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoProxy
@RequiredArgsConstructor
@ContextBootstrapper
public class PluginSecurityAspectContextBootstrapper {

    @Provide
    @ActivateWhenAnnotationPresent(EnableSpigotSecurity.class)
    SecurityAspect securityAspect(PlayerSession playerSession, SessionService sessionService) {
        return new SecurityAspect(playerSession, sessionService);
    }
}