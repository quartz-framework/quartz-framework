package xyz.quartzframework.core.command.picocli;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine;
import xyz.quartzframework.core.annotation.Injectable;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.bean.factory.PluginBeanFactory;

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