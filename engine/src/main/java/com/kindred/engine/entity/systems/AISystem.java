package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.resource.AnimationDataRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

@Slf4j
public class AISystem implements System {
    private final EntityManager entityManager;
    private int playerEntityId = -1;
    private PositionComponent playerPosCache = null;
    private final AnimationDataRegistry animationRegistry;

    public AISystem(EntityManager entityManager, AnimationDataRegistry animationRegistry) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        this.animationRegistry = animationRegistry;
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
                playerEntityId = -1;
                playerPosCache = null;
            }
        } else {
             playerPosCache = entityManager.getComponent(playerEntityId, PositionComponent.class);
             if (playerPosCache == null) playerEntityId = -1;
        }
        // --- End Find Player ---


        // --- Process AI Entities ---
        for (int entity : entityManager.getEntitiesWith(
                PositionComponent.class,
                VelocityComponent.class,
                WanderAIComponent.class,
                AnimationComponent.class))
        {
            if (entity == playerEntityId) continue;
            if(entityManager.hasComponent(entity, DeadComponent.class)) continue;

            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            WanderAIComponent ai = entityManager.getComponent(entity, WanderAIComponent.class);
            AttackComponent attackComp = entityManager.getComponent(entity, AttackComponent.class);
            AnimationComponent animComp = entityManager.getComponent(entity, AnimationComponent.class);

            if (pos == null || vel == null || ai == null || animComp == null) continue;

            boolean isNpc = entityManager.hasComponent(entity, NPCComponent.class);
            boolean canSeePlayer = false;
            float distanceSqToPlayer = Float.MAX_VALUE;

            if (playerEntityId != -1 && playerPosCache != null) {
                 float dx = playerPosCache.x - pos.x;
                 float dy = playerPosCache.y - pos.y;
                 distanceSqToPlayer = dx * dx + dy * dy;
                 if (distanceSqToPlayer <= (ai.aggroRadius * ai.aggroRadius)) {
                     canSeePlayer = true;
                 }
            }

            // --- Stop movement/wandering if starting an attack ---
            if (animComp.isAttacking) {
                vel.vx = 0;
                vel.vy = 0;
                // AI continues with its attack animation, CombatSystem handles hit detection
                // AnimationSystem handles progressing the attack animation
                continue; // Skip other AI logic while attacking
            }


            // State Transition Logic
            if (canSeePlayer && !isNpc && ai.currentState != WanderAIComponent.AIState.ATTACKING) {
                // Player entered aggro range, and it's not an NPC -> Attack!
                log.debug("Entity {} detected player, switching to ATTACKING", entity);
                ai.currentState = WanderAIComponent.AIState.ATTACKING;
                vel.vx = 0; // Stop current movement
                vel.vy = 0;
            } else if ((!canSeePlayer || playerEntityId == -1) && !isNpc && ai.currentState == WanderAIComponent.AIState.ATTACKING) {
                // Player left aggro range (or disappeared), and it's not an NPC -> Stop attacking
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
                    // ... (existing wander logic to set vel.vx, vel.vy) ...
                    // Update direction for animation
                    if (vel.vx != 0 || vel.vy != 0) {
                        int newDirection = animComp.direction;
                        if (vel.vy < 0) newDirection = AnimationComponent.UP;
                        else if (vel.vy > 0) newDirection = AnimationComponent.DOWN;
                        else if (vel.vx < 0) newDirection = AnimationComponent.LEFT;
                        else if (vel.vx > 0) newDirection = AnimationComponent.RIGHT;
                        animComp.setDirection(newDirection);
                    }
                    float wanderDx = ai.targetX - pos.x;
                    float wanderDy = ai.targetY - pos.y;
                    double wanderDist = Math.sqrt(wanderDx * wanderDx + wanderDy * wanderDy);

                    if (wanderDist < ai.moveSpeed * deltaTime * 1.5f || wanderDist == 0) {
                        // Arrived at wander target
                        vel.vx = 0; vel.vy = 0;
                        pos.x = ai.targetX; pos.y = ai.targetY;
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

                    ai.targetX = playerPosCache.x;
                    ai.targetY = playerPosCache.y;

                    float attackDx = ai.targetX - pos.x;
                    float attackDy = ai.targetY - pos.y;
                    // Simplified direction update for AI (prioritize horizontal then vertical or vice-versa)
                    if (Math.abs(attackDx) > Math.abs(attackDy)) {
                        animComp.setDirection(attackDx > 0 ? AnimationComponent.RIGHT : AnimationComponent.LEFT);
                    } else {
                        animComp.setDirection(attackDy > 0 ? AnimationComponent.DOWN : AnimationComponent.UP);
                    }

                    // Check if entity can attack and if player is in range
                    if (attackComp != null && distanceSqToPlayer <= (attackComp.range * attackComp.range)) {
                        // In attack range - stop moving and try to attack
                        vel.vx = 0;
                        vel.vy = 0;

                        // Check attack cooldown
                        if (!animComp.isAttacking && attackComp.currentCooldown <= 0) {
                            animComp.isAttacking = true; // Set character state
                            String weaponType = "GENERIC_SLASH";

                            BufferedImage[][] allAttackEffectFrames = animationRegistry.getAttackAnimationFrames(weaponType, animComp.direction);
                            Map<Integer, List<Rectangle>> hitboxesForEffect = animationRegistry.getAttackHitboxes(weaponType, animComp.direction);
                            float frameDuration = animationRegistry.getAttackFrameDuration(weaponType);
                            int totalFramesInSequence = animationRegistry.getNumberOfAttackFrames(weaponType);

                            if (allAttackEffectFrames != null && animComp.direction < allAttackEffectFrames.length && allAttackEffectFrames[animComp.direction] != null) {
                                AttackVisualEffectComponent effectComp = new AttackVisualEffectComponent(
                                    allAttackEffectFrames[animComp.direction],
                                    hitboxesForEffect,
                                    frameDuration,
                                    totalFramesInSequence,
                                    animComp.direction
                                );
                                entityManager.addComponent(entity, effectComp);
                                entityManager.addComponent(entity, new AttackActionComponent());

                                AttackingStateComponent attackingState = entityManager.getComponent(entity, AttackingStateComponent.class);
                                if (attackingState == null) {
                                    attackingState = new AttackingStateComponent();
                                    entityManager.addComponent(entity, attackingState);
                                }
                                attackingState.clearHitTargets();

                                attackComp.currentCooldown = attackComp.attackCooldown;
                                log.debug("AI Entity {} started attack. Added AttackVisualEffectComponent.", entity);
                            } else {
                                log.warn("AI Entity {}: Could not retrieve attack EFFECT frames for weapon {} dir {}. Attack not initiated.", entity, weaponType, animComp.direction);
                                animComp.isAttacking = false; // Revert state
                            }
                        }
                    } else if (!animComp.isAttacking) { // Only move if not already in an attack animation
                        // Move towards player
                        double attackDist = Math.sqrt(distanceSqToPlayer);
                        if (attackDist > 0) {
                             vel.vx = (int) Math.round((attackDx / attackDist) * ai.moveSpeed);
                             vel.vy = (int) Math.round((attackDy / attackDist) * ai.moveSpeed);
                             if (vel.vx == 0 && attackDx != 0) vel.vx = (attackDx > 0) ? 1 : -1;
                             if (vel.vy == 0 && attackDy != 0) vel.vy = (attackDy > 0) ? 1 : -1;
                        } else {
                             vel.vx = 0;
                             vel.vy = 0;
                        }
                    }
                    break;
            }
        }
    }
}
