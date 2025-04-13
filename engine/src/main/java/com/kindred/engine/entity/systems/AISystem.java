package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.components.WanderAIComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;

public class AISystem implements System {
    private final EntityManager entityManager;

    public AISystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
    }

    // Assuming update takes a delta time in seconds for timer calculations
    @Override
    public void update(float deltaTime) {
        // Get all entities that have the necessary components for wandering
        for (int entity : entityManager.getEntitiesWith(
                PositionComponent.class,
                VelocityComponent.class,
                WanderAIComponent.class))
        {
            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            WanderAIComponent ai = entityManager.getComponent(entity, WanderAIComponent.class);

            // State Machine Logic
            switch (ai.currentState) {
                case IDLE:
                    // Decrease timer
                    ai.idleTimer -= deltaTime;
                    // Ensure velocity is zero while idle
                    vel.vx = 0;
                    vel.vy = 0;
                    // Check if idle time is over
                    if (ai.idleTimer <= 0) {
                        ai.pickNewWanderTarget(); // Pick a new destination
                        ai.currentState = WanderAIComponent.AIState.WANDERING; // Change state
                        // System.out.println("Entity " + entity + " finished idling, now wandering to (" + ai.targetX + "," + ai.targetY + ")"); // Debug
                    }
                    break;

                case WANDERING:
                    // Calculate vector towards target
                    float dx = ai.targetX - pos.x;
                    float dy = ai.targetY - pos.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    // Check if close enough to the target
                    // Use a small threshold instead of exact match due to potential float inaccuracies
                    float arrivalThreshold = ai.wanderSpeed * deltaTime * 1.5f; // Example threshold
                    if (distance < arrivalThreshold || distance == 0) {
                        // Arrived at target
                        vel.vx = 0; // Stop movement
                        vel.vy = 0;
                        pos.x = ai.targetX; // Snap to target to avoid overshooting
                        pos.y = ai.targetY;
                        ai.resetIdleTimer(); // Start new idle timer
                        ai.currentState = WanderAIComponent.AIState.IDLE; // Change state
                        // System.out.println("Entity " + entity + " reached target, now idling."); // Debug
                    } else {
                        // Not at target yet, set velocity towards target
                        // Normalize direction vector and multiply by speed
                        vel.vx = (int) Math.round((dx / distance) * ai.wanderSpeed);
                        vel.vy = (int) Math.round((dy / distance) * ai.wanderSpeed);
                        // Ensure minimum movement if speed is low or distance small but > threshold
                        if (vel.vx == 0 && dx != 0) vel.vx = (dx > 0) ? 1 : -1;
                        if (vel.vy == 0 && dy != 0) vel.vy = (dy > 0) ? 1 : -1;
                    }
                    break;
            }
        }
    }
}
