package dev.pk7r.spigot.starter.core.convert;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnMissingBean;

@AutoConfiguration(force = true)
public class ConvertAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BigDecimalConvertService.class)
    BigDecimalConvertService bigDecimalConvertService() {
        return new BigDecimalConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(BigIntegerConvertService.class)
    BigIntegerConvertService bigIntegerConvertService() {
        return new BigIntegerConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(BooleanConvertService.class)
    BooleanConvertService booleanConvertService() {
        return new BooleanConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(DoubleConvertService.class)
    DoubleConvertService doubleConvertService() {
        return new DoubleConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(DurationConvertService.class)
    DurationConvertService durationConvertService() {
        return new DurationConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(FloatConvertService.class)
    FloatConvertService floatConvertService() {
        return new FloatConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(InstantConvertService.class)
    InstantConvertService instantConvertService() {
        return new InstantConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(IntegerConvertService.class)
    IntegerConvertService integerConvertService() {
        return new IntegerConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(LocalDateTimeConvertService.class)
    LocalDateTimeConvertService localDateTimeConvertService() {
        return new LocalDateTimeConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(LocalTimeConvertService.class)
    LocalTimeConvertService localTimeConvertService() {
        return new LocalTimeConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(LocalDateConvertService.class)
    LocalDateConvertService localDateConvertService() {
        return new LocalDateConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(LongConvertService.class)
    LongConvertService longConvertService() {
        return new LongConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(StringConvertService.class)
    StringConvertService stringConvertService() {
        return new StringConvertService();
    }

    @Bean
    @ConditionalOnMissingBean(StringListConvertService.class)
    StringListConvertService stringListConvertService() {
        return new StringListConvertService();
    }
}