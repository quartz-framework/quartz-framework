package xyz.quartzframework.data.util;

import jakarta.persistence.Id;
import lombok.experimental.UtilityClass;
import xyz.quartzframework.data.entity.Identity;

import java.lang.reflect.Field;

@UtilityClass
public class IdentityUtil {

    public <T> Field findIdentityField(Class<T> modelClass) {
        for (Field field : modelClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Identity.class) || field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("No @Identity field found in " + modelClass.getName());
    }

    public <T, ID> ID extractId(T entity, Class<ID> idClass) {
        try {
            Field field = findIdentityField(entity.getClass());
            ID id = idClass.cast(field.get(entity));
            if (id == null) throw new IllegalStateException("Entity ID cannot be null");
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract ID", e);
        }
    }
}