package xyz.quartzframework.cli.picocli;

import xyz.quartzframework.cli.picocli.conversion.PicocliConverterService;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.IHelpSectionRenderer;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public CommandLine build(QuartzBeanFactory factory) {
        CommandLine commandLine = doBuild();
        overrideHelpRenderers(commandLine);
        overrideConverters(factory, commandLine);
        return commandLine;
    }

    private CommandLine doBuild() {
        CommandLine commandLine = new CommandLine(instance, commandFactory);
        subcommands.forEach((key, value) -> {
            if (commandLine.getSubcommands().containsKey(key)) {
                return;
            }
            commandLine.addSubcommand(key, value.doBuild());
        });
        return commandLine;
    }

    private void overrideConverters(QuartzBeanFactory beanFactory, CommandLine commandLine) {
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