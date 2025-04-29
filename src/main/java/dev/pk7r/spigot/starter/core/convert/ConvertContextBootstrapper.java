package dev.pk7r.spigot.starter.core.convert;

import dev.pk7r.spigot.starter.core.annotation.ContextBootstrapper;
import dev.pk7r.spigot.starter.core.annotation.NoProxy;
import dev.pk7r.spigot.starter.core.annotation.Provide;
import dev.pk7r.spigot.starter.core.condition.annotation.ActivateWhenBeanMissing;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@NoProxy
@ContextBootstrapper
public class ConvertContextBootstrapper {

    @Provide
    @ActivateWhenBeanMissing(ConversionService.class)
    ConversionService conversionService() {
        return new DefaultConversionService();
    }
}