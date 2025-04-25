package dev.pk7r.spigot.starter.core;

import co.aikar.commands.*;
import dev.pk7r.spigot.starter.core.factory.CommandFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
class DefaultCommandFactory implements CommandFactory {

    @Getter
    private final Plugin plugin;

    @Getter
    private final PaperCommandManager commandManager;

    @Override
    public void registerCommand(BaseCommand command) {
        commandManager.registerCommand(command);
    }

    @Override
    public void registerStaticCompletion(String id, Supplier<Collection<String>> supplier) {
        commandManager.getCommandCompletions().registerStaticCompletion(id, supplier);
    }

    @Override
    public void registerAsyncCompletion(String id, Function<BukkitCommandCompletionContext, Collection<String>> supplier) {
        commandManager.getCommandCompletions().registerAsyncCompletion(id, supplier::apply);
    }

    @Override
    public void addReplacement(String key, String replacement) {
        commandManager.getCommandReplacements().addReplacement(key, replacement);
    }

    @Override
    public <T> void registerContext(Class<T> clazz, Function<BukkitCommandExecutionContext, T> resolver) {
        commandManager.getCommandContexts().registerContext(clazz, resolver::apply);
    }

    @Override
    public <T> void addCondition(String id, Consumer<ConditionContext<BukkitCommandIssuer>> condition) {
        commandManager.getCommandConditions().addCondition(id, condition::accept);
    }
}