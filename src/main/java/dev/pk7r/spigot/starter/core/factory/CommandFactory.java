package dev.pk7r.spigot.starter.core.factory;

import co.aikar.commands.*;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CommandFactory {

    Plugin getPlugin();

    PaperCommandManager getCommandManager();

    void registerCommand(BaseCommand command);

    void registerStaticCompletion(String id, Supplier<Collection<String>> supplier);

    void registerAsyncCompletion(String id, Function<BukkitCommandCompletionContext, Collection<String>> supplier);

    void addReplacement(String key, String replacement);

    <T> void registerContext(Class<T> clazz, Function<BukkitCommandExecutionContext, T> resolver);

    <T> void addCondition(String id, Consumer<ConditionContext<BukkitCommandIssuer>> condition);
}
