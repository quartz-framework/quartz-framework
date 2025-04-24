package dev.pk7r.spigot.starter.model;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class BeanDefinition {

    @NonNull
    @Builder.Default
    private final UUID id = UUID.randomUUID();

    @NonNull
    private String name;

    @NonNull
    private BeanScope scope;

    @Setter
    private Object instance;

    @NonNull
    private Class<?> type;

    private boolean primary;

    private boolean internalBean;

    private boolean namedInstance;

    private Class<?> literalType;

}