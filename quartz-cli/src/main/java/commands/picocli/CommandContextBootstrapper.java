package commands.picocli;

import commands.SubCommand;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.annotation.AnnotationUtils;
import picocli.CommandLine;
import xyz.quartzframework.Inject;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.beans.support.BeanInjector;
import xyz.quartzframework.beans.support.annotation.Preferred;
import xyz.quartzframework.beans.support.annotation.Provide;
import xyz.quartzframework.common.Pair;
import xyz.quartzframework.ordered.Priority;
import xyz.quartzframework.stereotype.ContextBootstrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoProxy
@ContextBootstrapper
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommandContextBootstrapper {

    private final Map<String, Pair<Object, Map<String, Object>>> commands = new HashMap<>();

    private final QuartzBeanFactory quartzBeanFactory;

    @Provide
    @Priority(0)
    @Preferred
    CommandLineDefinition picocliCommandLine(CommandFactory factory) {
        val candidates = quartzBeanFactory.getBeansWithAnnotation(CommandLine.Command.class);
        val commandCandidates = new ArrayList<>(candidates.values());
        val unwrappedCandidates = commandCandidates.stream()
                .map(BeanInjector::unwrapIfProxy)
                .toList();
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
                            Function.identity(),
                            (a, b) -> a
                    ));
            commands.putIfAbsent(commandName, Pair.of(candidate, commandSubCommands));
        });
        Object mainCommand = new BaseCommand();
        return register(new CommandLineDefinition(mainCommand, factory), factory);
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
                if (!mainCommand.getSubcommands().containsKey(subCommandName)) {
                    mainCommand.addSubcommand(subCommandName, subCommand);
                }
            });
            cli.addSubcommand(commandName, mainCommand);
        });
        return cli;
    }
}

@CommandLine.Command
class BaseCommand implements Runnable {

    @Override
    public void run() {
    }
}

