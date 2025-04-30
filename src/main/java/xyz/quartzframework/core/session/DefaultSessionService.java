package xyz.quartzframework.core.session;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.quartzframework.core.annotation.Injectable;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;
import xyz.quartzframework.core.util.SenderUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoProxy
@Injectable
@RequiredArgsConstructor
@ActivateWhenBeanMissing(SessionService.class)
public class DefaultSessionService implements SessionService, Listener {

    private final PlayerSession session;

    private final SenderUtils senderUtils;

    private final Map<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> current() {
        return of(session.getSender());
    }

    @Override
    public Map<String, Object> of(CommandSender sender) {
        val senderId = senderUtils.getSenderId(sender);
        if (senderId == null) {
            return null;
        }
        return sessions.computeIfAbsent(senderId, k -> new ConcurrentHashMap<>());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        val senderId = senderUtils.getSenderId(event.getPlayer());
        sessions.remove(senderId);
    }
}