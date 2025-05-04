package xyz.quartzframework.core.convert;

import lombok.val;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;

import java.util.Map;

@NoProxy
@ContextBootstrapper
public class ConvertContextBootstrapper {

    @Provide
    @ActivateWhenBeanMissing(ConversionService.class)
    ConversionService conversionService() {
        val service = new DefaultConversionService();
        service.addConverter(ConfigurationSection.class, Map.class, (s) -> s.getValues(true));
        return service;
    }
}