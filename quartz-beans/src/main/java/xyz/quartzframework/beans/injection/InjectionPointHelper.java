package xyz.quartzframework.beans.injection;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Field;
import java.util.function.Supplier;

@UtilityClass
public class InjectionPointHelper {

    public <R> R withInjectionPoint(Field field, boolean required, Supplier<R> resolve) {
        var dd = new DependencyDescriptor(field, required);
        InjectionPointContext.set(dd);
        try {
            return resolve.get();
        } finally {
            InjectionPointContext.clear();
        }
    }

    public <R> R withInjectionPoint(MethodParameter mp, boolean required, Supplier<R> resolve) {
        var dd = new DependencyDescriptor(mp, required);
        InjectionPointContext.set(dd);
        try {
            return resolve.get();
        } finally {
            InjectionPointContext.clear();
        }
    }
}
