package xyz.quartzframework.core.session;

import org.bukkit.Server;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Preferred;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.util.SenderUtils;

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
