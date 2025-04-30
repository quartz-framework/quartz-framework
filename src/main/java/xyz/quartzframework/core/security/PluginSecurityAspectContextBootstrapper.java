package xyz.quartzframework.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenAnnotationPresent;
import xyz.quartzframework.core.session.PlayerSession;
import xyz.quartzframework.core.session.SessionService;

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