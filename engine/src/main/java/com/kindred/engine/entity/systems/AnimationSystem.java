package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.AnimationComponent;
import com.kindred.engine.entity.components.DeadComponent;
import com.kindred.engine.entity.components.SpriteComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AnimationSystem implements System {

    private final EntityManager entityManager;

    public AnimationSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void update(float deltaTime) {
        update();
    }

    // Internal tick-based update logic
    public void update() {
        for (int entity : entityManager.getEntitiesWith(
                AnimationComponent.class,
                SpriteComponent.class,
                VelocityComponent.class
        )) {
            // Skip dead entities
            if (entityManager.hasComponent(entity, DeadComponent.class)) {
                continue;
            }

            SpriteComponent sprite = entityManager.getComponent(entity, SpriteComponent.class);
            AnimationComponent anim = entityManager.getComponent(entity, AnimationComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);

            if (sprite == null || anim == null || vel == null || anim.frames == null) continue;

            boolean moving = vel.vx != 0 || vel.vy != 0;
            int newDirection = anim.direction; // Default to current direction

            // --- Determine New Direction based on Velocity ---
            // Uses constants: DOWN=0, LEFT=1, RIGHT=2, UP=3
            // Prioritize vertical, then horizontal
            if (vel.vy < 0) {
                newDirection = AnimationComponent.UP; // Set to 3
            } else if (vel.vy > 0) {
                newDirection = AnimationComponent.DOWN; // Set to 0
            } else if (vel.vx < 0) {
                newDirection = AnimationComponent.LEFT; // Set to 1
            } else if (vel.vx > 0) {
                newDirection = AnimationComponent.RIGHT; // Set to 2
            }
            // -----------------------------------------------

            anim.setDirection(newDirection);

            // Update animation frame only if moving
            if (moving) {
                anim.update();
            } else {
                // Reset to first frame when stopped
                anim.frame = 0;
                anim.tick = 0;
            }

            sprite.sprite = anim.getCurrentFrame();
        }
    }
}
