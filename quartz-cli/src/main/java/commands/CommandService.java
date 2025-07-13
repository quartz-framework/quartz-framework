package commands;

import picocli.CommandLine;

import java.util.List;

public interface CommandService {

    boolean isRegistered();

    void registerCommand(CommandLine.Model.CommandSpec commandSpec);

    void unregisterCommand(CommandLine.Model.CommandSpec commandSpec);

    List<CommandLine.Model.CommandSpec> getCommands();
}
