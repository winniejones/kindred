package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.LifetimeComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * System responsible for decrementing the lifetime of entities
 * with a LifetimeComponent and destroying them when their lifetime expires.
 */
@Slf4j
public class LifetimeSystem implements System {

    private final EntityManager entityManager;

    public LifetimeSystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        log.info("LifetimeSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        List<Integer> entitiesToDestroy = new ArrayList<>();

        // Iterate through entities with a lifetime
        for (int entity : entityManager.getEntitiesWith(LifetimeComponent.class)) {
            LifetimeComponent lifetime = entityManager.getComponent(entity, LifetimeComponent.class);

            lifetime.remainingLifetime -= deltaTime;

            // If lifetime expired, mark for destruction
            if (lifetime.remainingLifetime <= 0) {
                entitiesToDestroy.add(entity);
            }
        }

        // Destroy entities whose lifetime expired
        for (int entityId : entitiesToDestroy) {
            log.trace("Destroying entity {} due to expired lifetime.", entityId);
            entityManager.destroyEntity(entityId);
        }
    }
}

