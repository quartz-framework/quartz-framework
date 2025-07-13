package xyz.quartzframework.data.util;

import lombok.experimental.UtilityClass;
import xyz.quartzframework.data.page.Sort;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;

@UtilityClass
public class SortUtil {

    @SuppressWarnings("unchecked")
    public <T> void sortList(List<T> list, Sort sort) {
        if (!sort.isSorted()) return;

        Comparator<T> combined = null;

        for (Sort.Order order : sort.getOrders()) {
            Comparator<T> comparator = (t1, t2) -> {
                try {
                    Field field = t1.getClass().getDeclaredField(order.property());
                    field.setAccessible(true);

                    Comparable<Object> v1 = (Comparable<Object>) field.get(t1);
                    Comparable<Object> v2 = (Comparable<Object>) field.get(t2);

                    if (v1 == null && v2 == null) return 0;
                    if (v1 == null) return -1;
                    if (v2 == null) return 1;

                    return v1.compareTo(v2);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to sort by property: " + order.property(), e);
                }
            };

            if (order.direction() == Sort.Direction.DESC) {
                comparator = comparator.reversed();
            }

            combined = (combined == null) ? comparator : combined.thenComparing(comparator);
        }
        list.sort(combined);
    }
}