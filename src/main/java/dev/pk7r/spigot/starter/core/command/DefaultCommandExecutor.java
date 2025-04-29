package dev.pk7r.spigot.starter.core.command;

import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Property;
import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;
import dev.pk7r.spigot.starter.core.command.picocli.CommandLineDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import picocli.CommandLine;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.apache.commons.lang.BooleanUtils.toBoolean;

@Slf4j
@NoProxy
@RequiredArgsConstructor
public class DefaultCommandExecutor implements CommandExecutor {

    private final PluginBeanFactory pluginBeanFactory;

    private final CommandLineDefinition commandLineDefinition;

    @Property("${spigot.messages.command_error:&cAn internal error occurred while attempting to perform this command}")
    private String commandErrorMessage;

    @Property("${spigot.messages.missing_parameter_error:&cMissing parameter: %s}")
    private String missingParameterErrorMessage;

    @Property("${spigot.messages.parameter_error:&cInvalid parameter: %s}")
    private String parameterErrorMessage;

    @Property("${spigot.commands.enable_cache:false}")
    private boolean cacheEnabled;

    private CommandLine commandLineCache;

    @Override
    public CommandResult execute(String... commandParts) {
        if (commandParts.length == 0) {
            return CommandResult.unknown();
        }
        try {
            if (!toBoolean(cacheEnabled) || commandLineCache == null) {
                commandLineCache = commandLineDefinition.build(pluginBeanFactory);
            }
            val output = new ArrayList<String>();
            val commands = commandLineCache.parseArgs(commandParts).asCommandLineList();
            if (commands.isEmpty()) {
                return CommandResult.unknown();
            }
            val commandLine = commands.get(commands.size() - 1);
            val command = commandLine.getCommand();
            if (command instanceof Runnable) {
                ((Runnable) command).run();
            } else if (command instanceof Callable) {
                val result = ((Callable<?>) command).call();
                output.addAll(buildOutput(result));
            }
            return new CommandResult(output);
        } catch (CommandLine.InitializationException ex) {
            log.error("Unexpected exception during command initialization", ex);
            return CommandResult.unknown();
        } catch (CommandLine.UnmatchedArgumentException ex) {
            val commandObject = ex.getCommandLine().getCommandSpec().userObject();
            if (commandObject == null){
                return CommandResult.unknown();
            }
            val message = String.format(parameterErrorMessage, String.join(", ", ex.getUnmatched()));
            return new CommandResult(ChatColor.translateAlternateColorCodes('&', message), true);
        } catch (CommandLine.MissingParameterException ex) {
            val message = String.format(missingParameterErrorMessage, ex.getMissing().get(0).paramLabel());
            return new CommandResult(ChatColor.translateAlternateColorCodes('&', message), true);
        } catch (CommandLine.ParameterException ex) {
            val message = String.format(parameterErrorMessage, ex.getArgSpec().paramLabel());
            return new CommandResult(ChatColor.translateAlternateColorCodes('&', message), true);
        } catch (CommandException ex) {
            return new CommandResult(ChatColor.RED + ex.getMessage(), true);
        } catch (Exception ex) {
            log.error("Unexpected exception while running /{}", StringUtils.join(commandParts, " "), ex);
            return new CommandResult(ChatColor.translateAlternateColorCodes('&', commandErrorMessage), true);
        }
    }

    private List<String> buildOutput(Object result) {
        if (result instanceof String) {
            return Collections.singletonList(ChatColor.translateAlternateColorCodes('&', (String) result));
        } else if (result instanceof Collection) {
            return ((Collection<?>) result)
                    .stream()
                    .flatMap(res -> buildOutput(res).stream())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}