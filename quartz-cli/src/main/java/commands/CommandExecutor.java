package commands;

public interface CommandExecutor {

    CommandResult execute(String... command);

}