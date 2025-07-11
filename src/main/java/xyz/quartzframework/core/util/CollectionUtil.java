package xyz.quartzframework.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.core.annotation.Order;
import xyz.quartzframework.core.bean.annotation.Priority;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CollectionUtil {

    public int getOrder(AnnotatedElement annotatedElement) {
        val order = annotatedElement.getAnnotation(Order.class);
        if (order == null) {
            val priority = annotatedElement.getAnnotation(Priority.class);
            if (priority == null) {
                return Integer.MAX_VALUE;
            }
            return priority.value();
        }
        return order.value();
    }

    public <T extends AnnotatedElement> List<T> reorder(Collection<T> unorderedElements) {
        return unorderedElements
                .stream()
                .sorted(Comparator.comparingInt(CollectionUtil::getOrder).reversed())
                .collect(Collectors.toList());
    }
}
