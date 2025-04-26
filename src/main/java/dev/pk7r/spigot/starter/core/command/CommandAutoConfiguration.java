package dev.pk7r.spigot.starter.core.command;

import co.aikar.commands.BaseCommand;
import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.bean.factory.BeanFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.PostConstruct;

@Slf4j
@RequiredArgsConstructor
@AutoConfiguration(force = true)
public class CommandAutoConfiguration {

    private final BeanFactory beanFactory;

    private final CommandFactory commandFactory;

    @PostConstruct
    public void postConstruct() {
        val commands = beanFactory.getBeansOfType(BaseCommand.class);
        commands.forEach(commandFactory::registerCommand);
        val commandManager = commandFactory.getCommandManager();
        val registeredCommandsAmount = commandManager.getRegisteredRootCommands().size();
        log.info("Registered {} commands", registeredCommandsAmount);
    }
}