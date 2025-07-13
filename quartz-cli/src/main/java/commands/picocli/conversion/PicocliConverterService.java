package commands.picocli.conversion;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.convert.ConversionService;
import picocli.CommandLine;
import xyz.quartzframework.Injectable;
import xyz.quartzframework.aop.NoProxy;

import java.util.Map;

@NoProxy
@Injectable
@RequiredArgsConstructor
public class PicocliConverterService {

    private final ConversionService conversionService;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void injectConverter(CommandLine commandLine) {
        val interpreterField = CommandLine.class.getDeclaredField("interpreter");
        interpreterField.setAccessible(true);
        val interpreter = interpreterField.get(commandLine);
        val interpreterClass = interpreterField.getType();
        val registryField = interpreterClass.getDeclaredField("converterRegistry");
        registryField.setAccessible(true);
        val originalRegistry = registryField.get(interpreter);
        if (!(originalRegistry instanceof ConverterRegistryDecorator)) {
            registryField.set(interpreter, new ConverterRegistryDecorator((Map<Class<?>,
                    CommandLine.ITypeConverter<?>>) originalRegistry, conversionService));
        }
        commandLine.getSubcommands().values().forEach(this::injectConverter);
    }
}