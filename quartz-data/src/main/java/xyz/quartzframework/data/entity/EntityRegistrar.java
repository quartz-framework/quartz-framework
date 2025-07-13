package xyz.quartzframework.data.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import xyz.quartzframework.data.util.IdentityUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class EntityRegistrar {

    @Getter
    private final List<EntityDefinition> entities = new ArrayList<>();

    public EntityRegistrar(EntityDiscovery entityDiscovery) {
        val entities = entityDiscovery.discover();
        for (Class<?> entity : entities) {
            try {
                val idType = IdentityUtil.findIdentityField(entity).getType();
                getEntities().add(new EntityDefinition(entity, idType, IdentityUtil.findIdentityField(entity), entity.getName(), entity.getSimpleName()));
                log.debug("Registered entity: {}", entity.getName());
            } catch (Exception e) {
                log.error("Failed to register entity class: {}", entity.getName(), e);
            }
        }
    }
}