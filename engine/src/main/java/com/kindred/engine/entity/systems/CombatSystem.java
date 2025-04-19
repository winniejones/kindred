package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
public class CombatSystem implements System {

    private final EntityManager entityManager;
    private static final float HIT_FLASH_DURATION = 0.15f;
    private static final float CORPSE_LIFETIME = 10.0f; // How long corpses last
    private final Random random = new Random();

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
            if (entityManager.hasComponent(entity, DeadComponent.class)) continue; // Skip dead
            AttackComponent attackComp = entityManager.getComponent(entity, AttackComponent.class);
            if (attackComp != null && attackComp.currentCooldown > 0) {
                attackComp.currentCooldown -= deltaTime;
                if (attackComp.currentCooldown < 0) {
                    attackComp.currentCooldown = 0;
                }
            }
        }

        // --- 2. Process Attack Actions ---
        List<Integer> attackers = new ArrayList<>(entityManager.getEntitiesWith(AttackActionComponent.class));
        for (int attackerId : attackers) {
            // Double-check components still exist
            if (!entityManager.isEntityActive(attackerId) ||
                    entityManager.hasComponent(attackerId, DeadComponent.class) ||
                    !entityManager.hasComponent(attackerId, AttackActionComponent.class) ||
                    !entityManager.hasComponent(attackerId, PositionComponent.class) ||
                    !entityManager.hasComponent(attackerId, AttackComponent.class))
            {
                entityManager.removeComponent(attackerId, AttackActionComponent.class);
                continue;
            }

            PositionComponent attackerPos = entityManager.getComponent(attackerId, PositionComponent.class);
            AttackComponent attackerAttack = entityManager.getComponent(attackerId, AttackComponent.class);

            log.debug("Processing attack action for entity {}", attackerId);

            // --- Find Targets ---
            // Simple distance check for now. Iterate all entities with Health.
            // TODO: Replace with more efficient spatial query or collision check later.
            // TODO: Determine attack direction/area instead of just radius.
            Set<Integer> potentialTargets = entityManager.getEntitiesWith(HealthComponent.class, PositionComponent.class, ColliderComponent.class);
            for (int targetId : potentialTargets) {
                // --- Validate Target ---
                if (attackerId == targetId) continue;
                if (entityManager.hasComponent(targetId, DeadComponent.class)) continue;

                // --- Check Faction Alignment ---
                boolean attackerIsPlayer = entityManager.hasComponent(attackerId, PlayerComponent.class);
                boolean attackerIsEnemy = entityManager.hasComponent(attackerId, EnemyComponent.class);
                boolean targetIsPlayer = entityManager.hasComponent(targetId, PlayerComponent.class);
                boolean targetIsEnemy = entityManager.hasComponent(targetId, EnemyComponent.class);

                if ((attackerIsPlayer && !targetIsEnemy) || (attackerIsEnemy && !targetIsPlayer)) {
                    // log.trace("Skipping target {} for attacker {}: Incorrect faction", targetId, attackerId);
                    continue;
                }
                PositionComponent targetPos = entityManager.getComponent(targetId, PositionComponent.class);
                ColliderComponent targetCollider = entityManager.getComponent(targetId, ColliderComponent.class);
                if (targetPos == null || targetCollider == null) continue;

                // Simple Distance Check
                float dx = targetPos.x - attackerPos.x;
                float dy = targetPos.y - attackerPos.y;
                float distanceSq = dx * dx + dy * dy;
                float rangeSq = attackerAttack.range * attackerAttack.range;

                if (distanceSq <= rangeSq) {
                    // Target is in range - Apply Damage
                    HealthComponent targetHealth = entityManager.getComponent(targetId, HealthComponent.class);
                    float previousHealth = targetHealth.currentHealth;
                    targetHealth.currentHealth = Math.max(0, targetHealth.currentHealth - attackerAttack.damage);
                    log.info("Entity {} hit Entity {} for {} damage. Health: {} -> {}", attackerId, targetId, attackerAttack.damage, previousHealth, targetHealth.currentHealth);

                    // Add TookDamageComponent for visual feedback
                    entityManager.addComponent(targetId, new TookDamageComponent(HIT_FLASH_DURATION));

                    // Spawn Hit Particles
                    int hitX = targetPos.x + targetCollider.offsetX + targetCollider.hitboxWidth / 2;
                    int hitY = targetPos.y + targetCollider.offsetY + targetCollider.hitboxHeight / 2;
                    // <<< Pass hit location to spawn method >>>
                    spawnHitParticles(hitX, hitY, 5 + random.nextInt(6));

                    // --- Check for death ---
                    if (targetHealth.currentHealth <= 0 && !entityManager.hasComponent(targetId, DeadComponent.class)) {
                        log.info("Entity {} died.", targetId);
                        // Add DeadComponent (defaults stage to 0)
                        entityManager.addComponent(targetId, new DeadComponent());

                        // Remove interaction components
                        entityManager.removeComponent(targetId, ColliderComponent.class);
                        entityManager.removeComponent(targetId, WanderAIComponent.class); // Or other AI
                        entityManager.removeComponent(targetId, AttackComponent.class);
                        entityManager.removeComponent(targetId, AttackActionComponent.class);
                        entityManager.removeComponent(targetId, TookDamageComponent.class);

                        // Optional: Stop movement if dead
                        if(entityManager.hasComponent(targetId, VelocityComponent.class)) {
                            VelocityComponent targetVel = entityManager.getComponent(targetId, VelocityComponent.class);
                            if (targetVel != null) {
                                targetVel.vx = 0;
                                targetVel.vy = 0;
                            }
                        }

                        // <<< Set INITIAL Corpse Sprite (Stage 0) >>>
                        SpriteComponent spriteComp = entityManager.getComponent(targetId, SpriteComponent.class);
                        if (spriteComp != null) {
                             // Get the stage 0 sprite (CorpseDecaySystem needs access too - refactor needed?)
                             // For now, assume CorpseDecaySystem loads them and maybe CombatSystem can access them?
                             // Simpler: Just leave the sprite as is for now, CorpseDecaySystem will set stage 0 on its first run.
                             // OR: Load stage 0 sprite here explicitly. Let's do that for clarity.
                             BufferedImage corpseSprite = getInitialCorpseSprite(targetId); // Get stage 0 sprite & Check if not placeholder
                             if (corpseSprite != null && corpseSprite.getWidth() > 1) {
                                 spriteComp.sprite = corpseSprite;
                                 log.debug("Set initial corpse sprite for entity {}", targetId);
                             } else {
                                 log.warn("Could not get initial corpse sprite for entity {}, might remain as last living sprite or placeholder.", targetId);
                                 // Optionally set to a generic placeholder if getInitialCorpseSprite failed badly
                                 // spriteComp.sprite = AssetLoader.createPlaceholderImage(32,32);
                             }
                        }
                        // <<< Add Lifetime Component for Corpse Removal >>>
                        entityManager.addComponent(targetId, new LifetimeComponent(CORPSE_LIFETIME));
                        log.debug("Added LifetimeComponent to entity {} (corpse).", targetId);

                    } // End death check
                } // End if in range
            } // End target loop
            // --- Remove Action Component ---
            entityManager.removeComponent(attackerId, AttackActionComponent.class);
        } // End attacker loop
    } // End update()

    /**
     * Helper method to spawn multiple particle entities at a location.
     * Creates particles with Position, Velocity, Particle, Lifetime, and ParticlePhysics components.
     * @param x Center X coordinate for particle spawn.
     * @param y Center Y coordinate for particle spawn.
     * @param count Number of particles to spawn.
     */
    private void spawnHitParticles(int x, int y, int count) {
        log.trace("Spawning {} particles at ({}, {})", count, x, y);
        int particleColor = 0xFFFF2222; // Reddish
        float lifetime = 0.3f + random.nextFloat() * 0.4f; // Lifetime 0.3 - 0.7 seconds
        float maxInitialSpeed = 80.0f; // Max initial speed in pixels/sec
        float initialZ = 2.0f; // Spawn slightly above ground
        float maxInitialVZ = 120.0f; // Max initial upward velocity pixels/sec

        for (int i = 0; i < count; i++) {
            int particleEntity = entityManager.createEntity();
            int particleSize = 1 + random.nextInt(3); // Size 1, 2 or 3

            entityManager.addComponent(particleEntity, new PositionComponent(x + random.nextInt(5)-2, y + random.nextInt(5)-2)); // Position

            double angle = random.nextDouble() * 2.0 * Math.PI; // Velocity
            float speed = random.nextFloat() * maxInitialSpeed;
            int vx = (int) (Math.cos(angle) * speed);
            int vy = (int) (Math.sin(angle) * speed);
            entityManager.addComponent(particleEntity, new VelocityComponent(vx, vy));

            entityManager.addComponent(particleEntity, new ParticleComponent(particleColor, particleSize)); // Visuals
            entityManager.addComponent(particleEntity, new LifetimeComponent(lifetime)); // Lifetime

            // --- Corrected Particle Physics Component Addition ---
            // Create using default constructor, then set initial Z and Vz
            ParticlePhysicsComponent physicsComp = new ParticlePhysicsComponent(); // Use default constructor
            physicsComp.z = initialZ;  // Set initial Z height
            physicsComp.vz = random.nextFloat() * maxInitialVZ; // Set initial upward velocity
            entityManager.addComponent(particleEntity, physicsComp); // Add the configured component
            // ------------------------------------------------------
        }
    }

    /**
     * Helper method to get the INITIAL corpse sprite (stage 0).
     * TODO: Refactor sprite loading/access - maybe AssetLoader provides these?
     */
    private BufferedImage getInitialCorpseSprite(int entityId) {
        // Determine the type key based on components
        String typeKey = "UNKNOWN";
        if (entityManager.hasComponent(entityId, EnemyComponent.class))
            // TODO: Add more specific checks if you have multiple enemy types with different corpses
            typeKey = "ENEMY_DEIDARA"; // Example
        // else if (entityManager.hasComponent(entityId, NPCComponent.class)) typeKey = "NPC_VILLAGER"; // Example

        if (!typeKey.equals("UNKNOWN")) {
            // Call the static method in CorpseDecaySystem to get the stage 0 sprite
            return CorpseDecaySystem.getDecaySprite(typeKey, 0);
        } else {
            log.warn("Could not determine corpse type for entity {}", entityId);
            return null; // Indicate no specific corpse sprite found
        }
    }
}
