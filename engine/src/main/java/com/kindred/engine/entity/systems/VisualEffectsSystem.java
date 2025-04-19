package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.TookDamageComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VisualEffectsSystem implements System {

    private final EntityManager entityManager;

    public VisualEffectsSystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        log.info("VisualEffectsSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        // Use a list to collect entities whose component should be removed,
        // to avoid ConcurrentModificationException if removing while iterating the set.
        List<Integer> entitiesToRemoveComponent = new ArrayList<>();

        // Iterate through entities with the TookDamageComponent
        for (int entity : entityManager.getEntitiesWith(TookDamageComponent.class)) {
            TookDamageComponent damageEffect = entityManager.getComponent(entity, TookDamageComponent.class);

            // Decrease the timer
            damageEffect.effectTimer -= deltaTime;

            // If the timer runs out, mark component for removal
            if (damageEffect.effectTimer <= 0) {
                entitiesToRemoveComponent.add(entity);
            }
        }

        // Remove the component from entities whose timers expired
        for (int entityId : entitiesToRemoveComponent) {
            entityManager.removeComponent(entityId, TookDamageComponent.class);
            log.trace("Removed TookDamageComponent from entity {}", entityId);
        }
    }
}
