package com.kindred.engine.entity.core;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class EntityManager {
    private int nextEntityId = 0;
    private final Map<Integer, Map<Class<? extends Component>, Component>> entities = new HashMap<>();

    /**
     * Creates a new entity with a unique ID.
     * @return The unique ID of the newly created entity.
     */
    public int createEntity() {
        int entityId = nextEntityId++;
        entities.put(entityId, new HashMap<>());
        log.trace("Entity created: {}", entityId);
        return entityId;
    }

    /**
     * Destroys an entity, removing it and all its components.
     * @param entityId The ID of the entity to destroy.
     */
    public void destroyEntity(int entityId) {
        if (entities.remove(entityId) != null) {
            // activeEntities.remove(entityId); // If using separate set
            log.trace("Entity destroyed: {}", entityId);
        } else {
            log.warn("Attempted to destroy non-existent entity: {}", entityId);
        }
        // Note: In more complex engines, might need to notify systems
        // or handle pooled entities before removal.
    }

    /**
     * Checks if an entity with the given ID currently exists.
     * @param entityId The entity ID to check.
     * @return true if the entity exists, false otherwise.
     */
    public boolean isEntityActive(int entityId) {
        // Simply checks if the ID is a key in the main map
        return entities.containsKey(entityId);
    }

    /**
     * Adds a component instance to the specified entity.
     * Replaces existing component of the same type.
     * @param entityId The ID of the entity.
     * @param component The component instance to add.
     * @param <T> The type of the component.
     */
    public <T extends Component> void addComponent(int entityId, T component) {
        Map<Class<? extends Component>, Component> entityComponents = entities.get(entityId);
        if (entityComponents != null) {
            // Use the specific class of the instance being added as the key
            entityComponents.put(component.getClass(), component);
            log.trace("Added component {} to entity {}", component.getClass().getSimpleName(), entityId);
        } else {
            log.warn("Attempted to add component {} to non-existent entity {}", component.getClass().getSimpleName(), entityId);
        }
    }

    /**
     * Removes a component of the specified type from an entity.
     * Needed by systems like CombatSystem to remove temporary marker components.
     *
     * @param entityId The ID of the entity.
     * @param componentClass The Class object representing the component type to remove.
     */
    public void removeComponent(int entityId, Class<? extends Component> componentClass) {
        Map<Class<? extends Component>, Component> entityComponents = entities.get(entityId);
        if (entityComponents != null) {
            if (entityComponents.remove(componentClass) != null) {
                log.trace("Removed component {} from entity {}", componentClass.getSimpleName(), entityId);
            } else {
                // This warning might be noisy if systems try to remove components that might not exist
                // log.warn("Attempted to remove non-existent component {} from entity {}", componentClass.getSimpleName(), entityId);
            }
        } else {
            log.warn("Attempted to remove component {} from non-existent entity {}", componentClass.getSimpleName(), entityId);
        }
    }

    /**
     * Retrieves a component instance of the specified type for a given entity.
     * @param entityId The ID of the entity.
     * @param componentClass The Class object representing the component type.
     * @param <T> The type of the component.
     * @return The component instance, or null if the entity or component doesn't exist.
     */
    public <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
        Map<Class<? extends Component>, Component> entityComponents = entities.get(entityId);
        if (entityComponents != null) {
            // Cast is safe because we store components keyed by their class
            return componentClass.cast(entityComponents.get(componentClass));
        }
        return null; // Entity doesn't exist
    }

    /**
     * Checks if an entity has a component of the specified type.
     * @param entityId The ID of the entity.
     * @param componentClass The Class object representing the component type.
     * @return true if the entity exists and has the component, false otherwise.
     */
    public boolean hasComponent(int entityId, Class<? extends Component> componentClass) {
        Map<Class<? extends Component>, Component> entityComponents = entities.get(entityId);
        // Check both entity existence and component existence
        return entityComponents != null && entityComponents.containsKey(componentClass);
    }

    /**
     * Finds all entity IDs that possess ALL of the specified component types.
     * Note: Iterates through all entities - can be inefficient for very large entity counts.
     *
     * @param requiredComponents Varargs array of Class objects for required components.
     * @return An unmodifiable Set containing the IDs of matching entities. Returns empty set if none found.
     */
    public Set<Integer> getEntitiesWith(Class<? extends Component>... requiredComponents) {
        if (requiredComponents == null || requiredComponents.length == 0) {
            // Return all active entity IDs if no components are specified
            // return Collections.unmodifiableSet(activeEntities); // If using separate set
            return Collections.unmodifiableSet(entities.keySet()); // Return keys from map
        }

        Set<Integer> result = new HashSet<>();
        // Iterate through the entity map entries
        for (Map.Entry<Integer, Map<Class<? extends Component>, Component>> entry : entities.entrySet()) {
            int entityId = entry.getKey();
            Map<Class<? extends Component>, Component> entityComponents = entry.getValue();
            boolean hasAll = true;
            // Check if this entity has all required components
            for (Class<? extends Component> compClass : requiredComponents) {
                if (!entityComponents.containsKey(compClass)) {
                    hasAll = false;
                    break; // No need to check further components for this entity
                }
            }
            // If all required components were found, add the entity ID to the result set
            if (hasAll) {
                result.add(entityId);
            }
        }
        // Return an unmodifiable view of the result set
        return Collections.unmodifiableSet(result);
    }

    /**
     * Finds the first entity ID that possesses ALL of the specified component types.
     * Useful for singleton entities like Player or Camera.
     * Note: Iteration order is not guaranteed for HashMaps.
     *
     * @param requiredComponents Varargs array of Class objects for required components.
     * @return The Integer ID of the first matching entity, or null if none found.
     */
    public Integer getFirstEntityWith(Class<? extends Component>... requiredComponents) {
        // This implementation relies on the iteration order of getEntitiesWith, which isn't guaranteed.
        // For true singletons, consider storing their IDs separately if performance is critical.
        // However, for typical use cases (finding 'the' player), this is often sufficient.
        Set<Integer> matchingEntities = getEntitiesWith(requiredComponents);
        if (!matchingEntities.isEmpty()) {
            return matchingEntities.iterator().next(); // Get the first element from the set iterator
        }
        return null; // No matching entity found
    }
}
