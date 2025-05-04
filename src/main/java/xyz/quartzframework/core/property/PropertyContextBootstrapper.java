package xyz.quartzframework.core.property;

import org.springframework.core.convert.ConversionService;
import xyz.quartzframework.core.QuartzPlugin;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;

@NoProxy
@ContextBootstrapper
public class PropertyContextBootstrapper {

    @Provide
    @ActivateWhenBeanMissing(PropertySourceFactory.class)
    PropertySourceFactory propertySourceFactory(QuartzPlugin<?> plugin) {
        return new DefaultPropertySourceFactory(plugin);
    }

    @Provide
    @ActivateWhenBeanMissing(PropertyPostProcessor.class)
    PropertyPostProcessor propertyPostProcessor(PropertySourceFactory propertySourceFactory, ConversionService conversionService) {
        return new DefaultPropertyPostProcessor(propertySourceFactory, conversionService);
    }
}