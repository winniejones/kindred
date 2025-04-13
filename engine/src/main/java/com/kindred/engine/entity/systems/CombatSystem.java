package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class CombatSystem implements System {

    // Manual logger declaration if not using Lombok:
    // private static final Logger log = LoggerFactory.getLogger(CombatSystem.class);

    private final EntityManager entityManager;

    public CombatSystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        log.info("CombatSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        // --- 1. Reduce Attack Cooldowns ---
        for (int entity : entityManager.getEntitiesWith(AttackComponent.class)) {
            AttackComponent attackComp = entityManager.getComponent(entity, AttackComponent.class);
            if (attackComp.currentCooldown > 0) {
                attackComp.currentCooldown -= deltaTime;
                if (attackComp.currentCooldown < 0) {
                    attackComp.currentCooldown = 0; // Clamp to zero
                }
            }
        }

        // --- 2. Process Attack Actions ---
        // Create a list of attackers first to avoid issues if component removed during iteration
        List<Integer> attackers = new ArrayList<>(entityManager.getEntitiesWith(AttackActionComponent.class));

        for (int attackerId : attackers) {
            // Double-check components still exist
            if (!entityManager.hasComponent(attackerId, AttackActionComponent.class) ||
                    !entityManager.hasComponent(attackerId, PositionComponent.class) ||
                    !entityManager.hasComponent(attackerId, AttackComponent.class))
            {
                entityManager.removeComponent(attackerId, AttackActionComponent.class); // Clean up stray component
                continue;
            }

            PositionComponent attackerPos = entityManager.getComponent(attackerId, PositionComponent.class);
            AttackComponent attackerAttack = entityManager.getComponent(attackerId, AttackComponent.class);
            boolean attackerIsPlayer = entityManager.hasComponent(attackerId, PlayerComponent.class);
            boolean attackerIsEnemy = entityManager.hasComponent(attackerId, EnemyComponent.class);

            log.debug("Processing attack action for entity {}", attackerId);

            // --- Find Targets ---
            // Simple distance check for now. Iterate all entities with Health.
            // TODO: Replace with more efficient spatial query or collision check later.
            // TODO: Determine attack direction/area instead of just radius.
            Set<Integer> potentialTargets = entityManager.getEntitiesWith(HealthComponent.class, PositionComponent.class, ColliderComponent.class);

            for (int targetId : potentialTargets) {
                // Don't hit self
                if (attackerId == targetId) continue;
                // Don't hit already dead entities
                if (entityManager.hasComponent(targetId, DeadComponent.class)) continue;

                // Check faction alignment (simple example: players hit enemies, enemies hit players)
                boolean targetIsPlayer = entityManager.hasComponent(targetId, PlayerComponent.class);
                boolean targetIsEnemy = entityManager.hasComponent(targetId, EnemyComponent.class);

                if ((attackerIsPlayer && !targetIsEnemy) || (attackerIsEnemy && !targetIsPlayer)) {
                    // log.trace("Skipping target {} for attacker {}: Incorrect faction", targetId, attackerId);
                    continue; // Skip hitting friendlies or non-combatants
                }


                PositionComponent targetPos = entityManager.getComponent(targetId, PositionComponent.class);

                // Calculate distance (using center approximation or collider center)
                // For simplicity, using origin points for now.
                float dx = targetPos.x - attackerPos.x;
                float dy = targetPos.y - attackerPos.y;
                float distanceSq = dx * dx + dy * dy; // Use squared distance to avoid sqrt
                float rangeSq = attackerAttack.range * attackerAttack.range;

                if (distanceSq <= rangeSq) {
                    // Target is in range - Apply Damage
                    HealthComponent targetHealth = entityManager.getComponent(targetId, HealthComponent.class);
                    float previousHealth = targetHealth.currentHealth;
                    targetHealth.currentHealth -= attackerAttack.damage;

                    log.info("Entity {} hit Entity {} for {} damage. Health: {} -> {}",
                            attackerId, targetId, attackerAttack.damage, previousHealth, targetHealth.currentHealth);

                    // Check for death
                    if (targetHealth.currentHealth <= 0 && !entityManager.hasComponent(targetId, DeadComponent.class)) {
                        log.info("Entity {} died.", targetId);
                        entityManager.addComponent(targetId, new DeadComponent());
                        // Optional: Stop movement if dead
                        if(entityManager.hasComponent(targetId, VelocityComponent.class)) {
                            VelocityComponent targetVel = entityManager.getComponent(targetId, VelocityComponent.class);
                            targetVel.vx = 0;
                            targetVel.vy = 0;
                        }
                        // Other systems (Render, AI) might react to DeadComponent
                    }
                }
            }

            // --- Remove Action Component ---
            // Attack action is processed, remove the marker component
            entityManager.removeComponent(attackerId, AttackActionComponent.class);
        } // End loop through attackers


        // --- 3. Optional: Handle Dead Entities (e.g., start despawn timer) ---
        // Could be done here or in a separate CleanupSystem
        // for (int entity : entityManager.getEntitiesWith(DeadComponent.class)) {
        // Add a DespawnTimerComponent or similar?
        // }

    } // End update()
}
