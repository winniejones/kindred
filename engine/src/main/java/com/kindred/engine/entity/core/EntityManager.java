package com.kindred.engine.entity.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityManager {
    private int nextEntityId = 0;
    private final Map<Integer, Map<Class<? extends Component>, Component>> entities = new HashMap<>();

    public int createEntity() {
        int entityId = nextEntityId++;
        entities.put(entityId, new HashMap<>());
        return entityId;
    }

    public <T extends Component> void addComponent(int entityId, T component) {
        entities.get(entityId).put(component.getClass(), component);
    }

    public <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
        return componentClass.cast(entities.get(entityId).get(componentClass));
    }

    public boolean hasComponent(int entityId, Class<? extends Component> componentClass) {
        return entities.get(entityId).containsKey(componentClass);
    }

    public Set<Integer> getEntitiesWith(Class<? extends Component>... requiredComponents) {
        Set<Integer> result = new HashSet<>();
        for (var entry : entities.entrySet()) {
            boolean hasAll = true;
            for (var comp : requiredComponents) {
                if (!entry.getValue().containsKey(comp)) {
                    hasAll = false;
                    break;
                }
            }
            if (hasAll) result.add(entry.getKey());
        }
        return result;
    }

    public Integer getFirstEntityWith(Class<? extends Component>... requiredComponents) {
        for (int entity : getEntitiesWith(requiredComponents)) {
            return entity;
        }
        return null;
    }
}
