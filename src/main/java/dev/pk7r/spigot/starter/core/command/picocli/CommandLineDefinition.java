package dev.pk7r.spigot.starter.core.command.picocli;

import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;
import dev.pk7r.spigot.starter.core.command.picocli.conversion.PicocliConverterService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.IHelpSectionRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class CommandLineDefinition {

    private final Object instance;

    private final CommandFactory commandFactory;

    private final HashMap<String, CommandLineDefinition> subcommands = new HashMap<>();

    CommandLineDefinition(Object instance, CommandFactory factory) {
        this.instance = instance;
        this.commandFactory = factory;
    }

    void addSubcommand(String name, Object commandLine) {
        if (commandLine instanceof CommandLineDefinition) {
            subcommands.put(name, (CommandLineDefinition) commandLine);
        } else {
            subcommands.put(name, new CommandLineDefinition(commandLine, commandFactory));
        }
    }

    public CommandLine build(PluginBeanFactory factory) {
        CommandLine commandLine = doBuild();
        overrideHelpRenderers(commandLine);
        overrideConverters(factory, commandLine);
        return commandLine;
    }

    private CommandLine doBuild() {
        CommandLine commandLine = new CommandLine(instance, commandFactory);
        subcommands.forEach((key, value) -> commandLine.addSubcommand(key, value.doBuild()));
        return commandLine;
    }

    private void overrideConverters(PluginBeanFactory beanFactory, CommandLine commandLine) {
        PicocliConverterService picocliConverterService = beanFactory.getBean(PicocliConverterService.class);
        picocliConverterService.injectConverter(commandLine);
    }

    private void overrideHelpRenderers(CommandLine commandLine) {
        Map<String, IHelpSectionRenderer> renderers = commandLine.getHelpSectionMap().keySet()
                .stream()
                .collect(Collectors.toMap(Function.identity(), (k) -> overrideRenderer(commandLine.getHelpSectionMap().get(k))));
        commandLine.setHelpSectionMap(renderers);
    }

    private IHelpSectionRenderer overrideRenderer(IHelpSectionRenderer renderer) {
        return (h) -> renderer.render(h).replaceAll("\\r", "");
    }
}