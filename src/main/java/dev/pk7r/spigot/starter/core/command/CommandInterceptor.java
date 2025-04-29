package dev.pk7r.spigot.starter.core.command;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.session.PlayerSession;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

@NoProxy
@RequiredArgsConstructor
public class CommandInterceptor implements Listener {

    private final PlayerSession session;

    private final CommandExecutor commandExecutor;

    private final CommandService commandService;

    @EventHandler
    void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled() || commandService.isRegistered()) return;
        val player = session.getPlayer();
        val result = commandExecutor.execute(event.getMessage().substring(1).split(" "));
        event.setCancelled(result.isExists());
        result.getOutput().forEach(player::sendMessage);
    }

    @EventHandler
    void onServerCommand(ServerCommandEvent event) {
        if (event.isCancelled() || commandService.isRegistered()) return;
        val sender = session.getSender();
        val result = commandExecutor.execute(event.getCommand().split(" "));
        event.setCancelled(result.isExists());
        result.getOutput().forEach(sender::sendMessage);
    }
}