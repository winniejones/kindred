package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AISystem implements System {
    private final EntityManager entityManager;
    private int playerEntityId = -1; // Cache player ID for efficiency
    private PositionComponent playerPosCache = null; // Cache player position

    public AISystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        log.info("AISystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        // --- Find Player (Cache for efficiency) ---
        // If we don't have the player ID, or the cached ID is no longer active, find it again.
        if (playerEntityId == -1 || !entityManager.isEntityActive(playerEntityId)) {
            Integer foundPlayer = entityManager.getFirstEntityWith(PlayerComponent.class, PositionComponent.class);
            if (foundPlayer != null) {
                playerEntityId = foundPlayer;
                playerPosCache = entityManager.getComponent(playerEntityId, PositionComponent.class);
                log.debug("AISystem found player entity: {}", playerEntityId);
            } else {
                playerEntityId = -1; // Ensure it's reset if player disappears
                playerPosCache = null;
                // log.trace("AISystem: Player not found this cycle.");
                // No player, AI can only wander/idle
            }
        } else {
             // If we have a valid ID, just update the position cache
             // Check if player still has position component (might have died but not removed?)
             playerPosCache = entityManager.getComponent(playerEntityId, PositionComponent.class);
             if (playerPosCache == null) playerEntityId = -1; // Player lost position? Reset.
        }
        // --- End Find Player ---


        // --- Process AI Entities ---
        for (int entity : entityManager.getEntitiesWith(
                PositionComponent.class,
                VelocityComponent.class,
                WanderAIComponent.class)) // Process all entities with basic wander/attack AI
        {
            // Skip player if player somehow has WanderAIComponent
            if (entity == playerEntityId) continue;
            if(entityManager.hasComponent(entity, DeadComponent.class)) continue;

            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            WanderAIComponent ai = entityManager.getComponent(entity, WanderAIComponent.class);
            AttackComponent attackComp = entityManager.getComponent(entity, AttackComponent.class); // Might be null if AI can't attack

            boolean canSeePlayer = false;
            float distanceSqToPlayer = Float.MAX_VALUE;

            // Check distance to player if player exists
            if (playerEntityId != -1 && playerPosCache != null) {
                 float dx = playerPosCache.x - pos.x;
                 float dy = playerPosCache.y - pos.y;
                 distanceSqToPlayer = dx * dx + dy * dy;
                 if (distanceSqToPlayer <= (ai.aggroRadius * ai.aggroRadius)) {
                     canSeePlayer = true;
                 }
            }

            // --- State Transition Logic ---
            if (canSeePlayer && ai.currentState != WanderAIComponent.AIState.ATTACKING) {
                // Player entered aggro range while AI was idle/wandering
                log.debug("Entity {} detected player, switching to ATTACKING", entity);
                ai.currentState = WanderAIComponent.AIState.ATTACKING;
                vel.vx = 0; // Stop current movement
                vel.vy = 0;
            } else if (!canSeePlayer && ai.currentState == WanderAIComponent.AIState.ATTACKING) {
                // Player left aggro range while AI was attacking
                log.debug("Entity {} lost player, switching to IDLE", entity);
                ai.currentState = WanderAIComponent.AIState.IDLE;
                ai.resetIdleTimer(); // Start idling
                vel.vx = 0; // Stop movement
                vel.vy = 0;
            }
            // --- End State Transition Logic ---


            // --- State Action Logic ---
            switch (ai.currentState) {
                case IDLE:
                    ai.idleTimer -= deltaTime;
                    // Ensure velocity is zero while idle
                    vel.vx = 0;
                    vel.vy = 0;
                    // Check if idle time is over
                    if (ai.idleTimer <= 0) {
                        ai.pickNewWanderTarget();
                        ai.currentState = WanderAIComponent.AIState.WANDERING;
                        log.trace("Entity {} finished idling, wandering to ({}, {})", entity, ai.targetX, ai.targetY);
                    }
                    break;

                case WANDERING:
                    // Move towards wander target (ai.targetX, ai.targetY)
                    float wanderDx = ai.targetX - pos.x;
                    float wanderDy = ai.targetY - pos.y;
                    double wanderDist = Math.sqrt(wanderDx * wanderDx + wanderDy * wanderDy);

                    if (wanderDist < ai.moveSpeed * deltaTime * 1.5f || wanderDist == 0) {
                        // Arrived at wander target
                        vel.vx = 0; vel.vy = 0;
                        pos.x = ai.targetX; pos.y = ai.targetY; // Snap
                        ai.resetIdleTimer();
                        ai.currentState = WanderAIComponent.AIState.IDLE;
                        log.trace("Entity {} reached wander target, now idling.", entity);
                    } else {
                        // Move towards wander target
                        vel.vx = (int) Math.round((wanderDx / wanderDist) * ai.moveSpeed);
                        vel.vy = (int) Math.round((wanderDy / wanderDist) * ai.moveSpeed);
                        if (vel.vx == 0 && wanderDx != 0) vel.vx = (wanderDx > 0) ? 1 : -1;
                        if (vel.vy == 0 && wanderDy != 0) vel.vy = (wanderDy > 0) ? 1 : -1;
                    }
                    break;

                case ATTACKING:
                    if (playerEntityId == -1 || playerPosCache == null) {
                         // Player disappeared mid-attack? Revert to idle.
                         ai.currentState = WanderAIComponent.AIState.IDLE;
                         ai.resetIdleTimer();
                         vel.vx = 0; vel.vy = 0;
                         log.debug("Entity {} lost player target while attacking, switching to IDLE", entity);
                         break; // Exit switch for this entity
                    }

                    // Target is the player's current position
                    ai.targetX = playerPosCache.x; // Update target continuously
                    ai.targetY = playerPosCache.y;

                    float attackDx = ai.targetX - pos.x;
                    float attackDy = ai.targetY - pos.y;
                    // distanceSqToPlayer already calculated above

                    // Check if entity can attack and if player is in range
                    if (attackComp != null && distanceSqToPlayer <= (attackComp.range * attackComp.range)) {
                        // In attack range - stop moving and try to attack
                        vel.vx = 0;
                        vel.vy = 0;

                        // Check attack cooldown
                        if (attackComp.currentCooldown <= 0) {
                            // Cooldown ready - initiate attack!
                            log.debug("Entity {} attacks player {}!", entity, playerEntityId);
                            entityManager.addComponent(entity, new AttackActionComponent()); // Signal CombatSystem
                            attackComp.currentCooldown = attackComp.attackCooldown; // Reset cooldown
                        } else {
                            // Still cooling down, wait
                            // log.trace("Entity {} attack on cooldown ({}s left)", entity, attackComp.currentCooldown);
                        }
                    } else {
                        // Out of attack range, but still aggroed - move towards player
                        double attackDist = Math.sqrt(distanceSqToPlayer); // Need actual distance for normalization
                        vel.vx = (int) Math.round((attackDx / attackDist) * ai.moveSpeed);
                        vel.vy = (int) Math.round((attackDy / attackDist) * ai.moveSpeed);
                        if (vel.vx == 0 && attackDx != 0) vel.vx = (attackDx > 0) ? 1 : -1;
                        if (vel.vy == 0 && attackDy != 0) vel.vy = (attackDy > 0) ? 1 : -1;
                        // log.trace("Entity {} chasing player with velocity ({}, {})", entity, vel.vx, vel.vy);
                    }
                    break;
            } // End switch state
        } // End entity loop
    } // End update()
}
