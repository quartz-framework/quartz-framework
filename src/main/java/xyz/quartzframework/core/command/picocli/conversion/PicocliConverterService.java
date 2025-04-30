package xyz.quartzframework.core.command.picocli.conversion;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.convert.ConversionService;
import picocli.CommandLine;
import xyz.quartzframework.core.annotation.Injectable;
import xyz.quartzframework.core.annotation.NoProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    @SneakyThrows
    private void removeFinal(Field field) {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

}