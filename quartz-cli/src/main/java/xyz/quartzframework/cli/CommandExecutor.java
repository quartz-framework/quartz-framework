package xyz.quartzframework.cli;

public interface CommandExecutor {

    CommandResult execute(String... command);

}