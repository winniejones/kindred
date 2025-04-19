package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.LifetimeComponent;
import com.kindred.engine.entity.components.ParticlePhysicsComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies gravity and handles ground bouncing/friction for particles
 * based on their Z-axis position and velocity stored in ParticlePhysicsComponent.
 */
@Slf4j
public class ParticlePhysicsSystem implements System {

    private final EntityManager entityManager;

    public ParticlePhysicsSystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        log.info("ParticlePhysicsSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        // Iterate entities with the necessary physics and velocity components
        for (int entity : entityManager.getEntitiesWith(
                VelocityComponent.class, // Needed for ground friction
                ParticlePhysicsComponent.class,
                LifetimeComponent.class // Ensure we only process active particles
        )) {
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            ParticlePhysicsComponent physics = entityManager.getComponent(entity, ParticlePhysicsComponent.class);

            // 1. Apply Gravity
            physics.vz -= physics.gravity * deltaTime;

            // 2. Update Z Position
            physics.z += physics.vz * deltaTime;

            // 3. Check for Ground Collision (Z < 0)
            if (physics.z < 0) {
                physics.z = 0; // Place back on ground

                // Apply bounce damping to Z velocity
                physics.vz *= physics.bounceDamping;

                // Apply ground friction damping to X and Y velocity
                vel.vx *= physics.groundFriction;
                vel.vy *= physics.groundFriction;

                // Optional: If vz becomes very small after bounce, stop bouncing to prevent jitter
                if (Math.abs(physics.vz) < 1.0f) { // Adjust threshold as needed
                    physics.vz = 0;
                }
                // Optional: Stop horizontal movement completely if friction makes it negligible
                if (Math.abs(vel.vx) < 0.1f) vel.vx = 0;
                if (Math.abs(vel.vy) < 0.1f) vel.vy = 0;

                // log.trace("Entity {} bounced. New vz={}, vx={}, vy={}", entity, physics.vz, vel.vx, vel.vy);
            }
        }
    }
}

