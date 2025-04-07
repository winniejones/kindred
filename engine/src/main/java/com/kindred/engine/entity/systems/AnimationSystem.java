package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.AnimationComponent;
import com.kindred.engine.entity.components.SpriteComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;

public class AnimationSystem implements System {

    private final EntityManager entityManager;

    public AnimationSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void update() {
        for (int entity : entityManager.getEntitiesWith(AnimationComponent.class, SpriteComponent.class, VelocityComponent.class)) {
            AnimationComponent anim = entityManager.getComponent(entity, AnimationComponent.class);
            SpriteComponent sprite = entityManager.getComponent(entity, SpriteComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);

            boolean moving = vel.vx != 0 || vel.vy != 0;

            // Determine direction based on velocity
            int direction = anim.direction;
            if (vel.vy < 0) direction = 2; // up
            if (vel.vy > 0) direction = 1; // down
            if (vel.vx < 0) direction = 0; // left
            if (vel.vx > 0) direction = 3; // right

            anim.setDirection(direction);

            if (moving) {
                anim.update();
            } else {
                anim.frame = 0;
                anim.tick = 0;
            }

            sprite.sprite = anim.getCurrentFrame();
        }
    }
}
