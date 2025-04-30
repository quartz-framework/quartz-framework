package xyz.quartzframework.core.convert;

import org.bukkit.configuration.ConfigurationSection;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import xyz.quartzframework.core.annotation.ContextBootstrapper;
import xyz.quartzframework.core.annotation.NoProxy;
import xyz.quartzframework.core.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;

import java.util.Map;

@NoProxy
@ContextBootstrapper
public class ConvertContextBootstrapper {

    @Provide
    @ActivateWhenBeanMissing(ConversionService.class)
    ConversionService conversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(ConfigurationSection.class, Map.class, (s) -> s.getValues(true));
        return conversionService;
    }
}