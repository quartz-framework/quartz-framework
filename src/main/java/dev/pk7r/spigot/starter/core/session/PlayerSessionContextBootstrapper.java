package dev.pk7r.spigot.starter.core.session;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Preferred;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.util.SenderUtils;
import org.bukkit.Server;

@NoProxy
@ContextBootstrapper
public class PlayerSessionContextBootstrapper {

    @Provide
    @Preferred
    SenderUtils senderUtils(Server server) {
        return new SenderUtils(server);
    }

    @Provide
    @Preferred
    PlayerSession session(SenderUtils senderUtils) {
        return new PlayerSession(senderUtils);
    }
}
