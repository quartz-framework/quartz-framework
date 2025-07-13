package xyz.quartzframework.data.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@UtilityClass
@Slf4j
public class GenericTypeUtil {

    public Class<?>[] resolve(Class<?> subType, Class<?> targetSuper) {
        return resolveFromInterfaces(subType.getGenericInterfaces(), targetSuper);
    }

    private Class<?>[] resolveFromInterfaces(Type[] interfaces, Class<?> targetSuper) {
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType pt) {
                if (pt.getRawType() instanceof Class<?> raw && targetSuper.isAssignableFrom(raw)) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length == 2 && args[0] instanceof Class<?> && args[1] instanceof Class<?>) {
                        return new Class<?>[] {
                                (Class<?>) args[0],
                                (Class<?>) args[1]
                        };
                    }
                }
                assert pt.getRawType() instanceof Class<?>;
                Class<?> rawClass = (Class<?>) pt.getRawType();
                Class<?>[] result = resolveFromInterfaces(rawClass.getGenericInterfaces(), targetSuper);
                if (result != null) return result;
            } else if (type instanceof Class<?> raw) {
                Class<?>[] result = resolveFromInterfaces(raw.getGenericInterfaces(), targetSuper);
                if (result != null) return result;
            }
        }
        return null;
    }
}