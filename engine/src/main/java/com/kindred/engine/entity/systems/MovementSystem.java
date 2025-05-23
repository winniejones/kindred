package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.DeadComponent;
import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;

public class MovementSystem implements System {
    private final EntityManager entityManager;

    public MovementSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void update(float deltaTime) {
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, VelocityComponent.class)) {
            if(entityManager.hasComponent(entity, DeadComponent.class)) continue;

            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);

            pos.x += vel.vx;
            pos.y += vel.vy;
        }
    }
}
