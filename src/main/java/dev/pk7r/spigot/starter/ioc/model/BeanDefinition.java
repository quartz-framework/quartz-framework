package dev.pk7r.spigot.starter.ioc.model;

import lombok.*;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class BeanDefinition {

    @NonNull
    private String name;

    @NonNull
    private BeanScope scope;

    private Object instance;

    @NonNull
    private Class<?> type;

    private Class<?> literalType;

}