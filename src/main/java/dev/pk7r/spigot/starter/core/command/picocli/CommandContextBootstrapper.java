package dev.pk7r.spigot.starter.core.command.picocli;

import dev.pk7r.spigot.starter.core.annotation.*;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenBeanMissing;
import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;
import dev.pk7r.spigot.starter.core.command.*;
import dev.pk7r.spigot.starter.core.common.Pair;
import dev.pk7r.spigot.starter.core.session.PlayerSession;
import dev.pk7r.spigot.starter.core.util.InjectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.annotation.AnnotationUtils;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@NoProxy
@ContextBootstrapper
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommandContextBootstrapper {

    private final Map<String, Pair<Object, Map<String, Object>>> commands = new HashMap<>();

    private final PluginBeanFactory pluginBeanFactory;

    @Provide
    @Priority(0)
    @Preferred
    CommandLineDefinition picocliCommandLine(CommandFactory factory) {
        val candidates = pluginBeanFactory.getBeansWithAnnotation(CommandLine.Command.class);
        val commandCandidates = new ArrayList<>(candidates.values());
        val unwrappedCandidates = commandCandidates.stream()
                .map(InjectionUtil::unwrapIfProxy)
                .collect(Collectors.toList());
        unwrappedCandidates.forEach(candidate -> {
            val candidateClass = candidate.getClass();
            if (!isMainCommand(candidateClass)) {
                return;
            }
            val commandName = getCommandName(candidateClass);
            val subcommands = getSubcommands(candidateClass);
            Map<String, Object> commandSubCommands = unwrappedCandidates.stream()
                    .filter(c -> isSubcommand(c.getClass()))
                    .filter(c -> Arrays.asList(subcommands).contains(c.getClass()))
                    .collect(Collectors.toMap(
                            c -> getCommandName(c.getClass()),
                            Function.identity()
                    ));
            commands.putIfAbsent(commandName, Pair.of(candidate, commandSubCommands));
        });
        Object mainCommand = new BaseCommand();
        return register(new CommandLineDefinition(mainCommand, factory), factory);
    }

    @Provide
    @Priority(1)
    @ActivateWhenBeanMissing(CommandExecutor.class)
    CommandExecutor commandExecutor(CommandLineDefinition commandLineDefinition) {
        return new DefaultCommandExecutor(pluginBeanFactory, commandLineDefinition);
    }

    @Provide
    @Priority(2)
    @Preferred
    CommandInterceptor commandInterceptor(PlayerSession session, CommandExecutor executor, CommandService service) {
        return new CommandInterceptor(session, executor, service);
    }

    private boolean isMainCommand(Class<?> type) {
        return isCommand(type) && !isSubcommand(type);
    }

    private boolean isCommand(Class<?> type) {
        return AnnotationUtils.findAnnotation(type, CommandLine.Command.class) != null;
    }

    private boolean isSubcommand(Class<?> type) {
        return isCommand(type) && AnnotationUtils.findAnnotation(type, SubCommand.class) != null;
    }

    private String getCommandName(Class<?> commandClass) {
        val annotation = commandClass.getAnnotation(CommandLine.Command.class);
        if (annotation == null) {
            throw new IllegalStateException("Command class " + commandClass.getName() + " is not annotated with @Command");
        }
        return annotation.name();
    }

    private Class<?>[] getSubcommands(Class<?> commandClass) {
        if (commandClass == null) {
            return new Class<?>[0];
        }
        val annotation = commandClass.getAnnotation(CommandLine.Command.class);
        if (annotation == null) {
            return new Class<?>[0];
        }
        return annotation.subcommands();
    }

    private CommandLineDefinition register(CommandLineDefinition cli, CommandFactory factory) {
        commands.forEach((commandName, pair) -> {
            Object commandInstance = pair.getFirst();
            Map<String, Object> subcommands = pair.getSecond();
            CommandLineDefinition mainCommand = new CommandLineDefinition(commandInstance, factory);
            subcommands.forEach((subCommandName, subCommandInstance) -> {
                CommandLineDefinition subCommand = new CommandLineDefinition(subCommandInstance, factory);
                mainCommand.addSubcommand(subCommandName, subCommand);
            });
            cli.addSubcommand(commandName, mainCommand);
        });
        return cli;
    }
}

@Injectable
@CommandLine.Command
class BaseCommand implements Runnable {

    @Override
    public void run() {
    }
}