package xyz.quartzframework.data.page;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class Sort {

    public enum Direction {
        ASC, DESC
    }

    public record Order(String property, Direction direction) {}

    private final List<Order> orders;

    private Sort(List<Order> orders) {
        this.orders = orders;
    }

    public static Sort by(String property, Direction direction) {
        return new Sort(List.of(new Order(property, direction)));
    }

    public static Sort unsorted() {
        return new Sort(Collections.emptyList());
    }

    public boolean isSorted() {
        return !orders.isEmpty();
    }
}