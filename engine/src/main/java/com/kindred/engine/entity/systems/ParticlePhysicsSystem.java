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
    private static final float VZ_STOP_THRESHOLD = 5.0f; // pixels/sec - Stop bouncing if vz is below this after bounce
    private static final float VX_VY_STOP_THRESHOLD = 1.0f; // pixels/sec - Stop sliding if vx/vy is below this


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
            if (vel == null || physics == null) continue;

            // 1. Apply Gravity
            if (physics.z > 0 || physics.vz > VZ_STOP_THRESHOLD * 0.5f) { // Apply if airborne or bouncing up
                physics.vz -= physics.gravity * deltaTime;
            }

            // 2. Update Z Position
            physics.z += physics.vz * deltaTime;

            // 3. Check for Ground Collision (Z <= 0)
            if (physics.z <= 0 && physics.vz < 0) { // Check vz < 0 ensures we only process on downward impact
                physics.z = 0; // Place exactly on ground

                // Apply bounce damping to Z velocity
                physics.vz *= physics.bounceDamping;

                // Apply ground friction damping to X and Y velocity ONCE on bounce
                vel.vx *= (int)(physics.groundFriction);
                vel.vy *= (int)(physics.groundFriction);

                // If bounce is very small, stop Z movement completely
                if (Math.abs(physics.vz) < VZ_STOP_THRESHOLD) {
                    physics.vz = 0;
                }

                // Ensure vx/vy become exactly 0 if friction makes them negligible
                if (Math.abs(vel.vx) < VX_VY_STOP_THRESHOLD) vel.vx = 0;
                if (Math.abs(vel.vy) < VX_VY_STOP_THRESHOLD) vel.vy = 0;

                // log.trace("Entity {} bounced. New vz={}, vx={}, vy={}", entity, physics.vz, vel.vx, vel.vy);
            } else if (physics.z <= 0 && physics.vz == 0) {
                // If already resting on the ground (z=0, vz=0), ensure vx/vy eventually stop due to friction
                // (This assumes friction was applied on the last bounce)
                // If vx/vy didn't reach zero, they might slide forever with int velocity.
                // We already check and zero them if they are <= 1 after bounce friction.
                // If more friction is needed for resting particles, logic could be added here.
                // For now, the bounce friction + threshold check should suffice.
            }

            // Ensure Z doesn't go below 0 even if vz was positive but deltaTime pushed it down
            if (physics.z < 0) physics.z = 0;
        }
    }
}

