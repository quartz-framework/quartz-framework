package xyz.quartzframework.common;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@Setter(AccessLevel.PRIVATE)
public class Pair<F, S> {

    private F first;

    private S second;

    private Pair(F first, S second) {
        setFirst(first);
        setSecond(second);
    }

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }
}