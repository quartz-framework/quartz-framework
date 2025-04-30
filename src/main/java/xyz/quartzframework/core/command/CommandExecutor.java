package xyz.quartzframework.core.command;

public interface CommandExecutor {

    CommandResult execute(String... command);

}