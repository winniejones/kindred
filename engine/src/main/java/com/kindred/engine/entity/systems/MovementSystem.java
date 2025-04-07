package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.core.EntityManager;

public class MovementSystem {
    private final EntityManager entityManager;

    public MovementSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void update() {
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, VelocityComponent.class)) {
            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);

            pos.x += vel.vx;
            pos.y += vel.vy;
        }
    }
}
