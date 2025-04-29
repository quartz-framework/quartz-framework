package dev.pk7r.spigot.starter.core.command.picocli;

import dev.pk7r.spigot.starter.core.annotation.Injectable;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.bean.factory.PluginBeanFactory;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@NoProxy
@Injectable
@RequiredArgsConstructor
public class CommandFactory implements CommandLine.IFactory {

    private final PluginBeanFactory pluginBeanFactory;

    @Override
    public <K> K create(Class<K> cls) {
        return pluginBeanFactory.getBean(cls);
    }
}