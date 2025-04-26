package dev.pk7r.spigot.starter.core.command;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.PaperCommandManager;
import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnMissingBean;
import dev.pk7r.spigot.starter.core.context.PluginContext;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
@AutoConfiguration(force = true)
public class CommandFactoryAutoConfiguration {

    private final PluginContext context;

    @Bean
    @ConditionalOnMissingBean(BukkitCommandManager.class)
    BukkitCommandManager commandManager(Plugin plugin) {
        return new PaperCommandManager(plugin);
    }

    @Bean
    @ConditionalOnMissingBean(CommandFactory.class)
    CommandFactory commandFactory(Plugin plugin, BukkitCommandManager commandManager) {
        return new DefaultCommandFactory(plugin, commandManager);
    }
}