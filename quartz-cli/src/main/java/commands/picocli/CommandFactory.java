package commands.picocli;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine;
import xyz.quartzframework.Injectable;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;

@NoProxy
@Injectable
@RequiredArgsConstructor
public class CommandFactory implements CommandLine.IFactory {

    private final QuartzBeanFactory quartzBeanFactory;

    @Override
    public <K> K create(Class<K> cls) {
        return quartzBeanFactory.getBean(cls);
    }
}