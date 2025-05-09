package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.resource.AssetLoader;
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

    // Corpse Sprites (ensure loaded)
    private static BufferedImage deidaraCorpseSprite = AssetLoader.loadImage("/assets/sprites/decaying_deidara_corpse.png");

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
                    !entityManager.hasComponent(attackerId, AttackComponent.class)) {
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
                    float actualDamage = attackerAttack.damage; // TODO: Factor in target defense, attacker stats
                    float previousHealth = targetHealth.currentHealth;
                    targetHealth.currentHealth = Math.max(0, targetHealth.currentHealth - actualDamage);
                    float damageDealt = previousHealth - targetHealth.currentHealth; // Actual damage applied

                    log.info("Entity {} hit Entity {} for {} damage. Health: {} -> {}", attackerId, targetId, actualDamage, previousHealth, targetHealth.currentHealth);
                    entityManager.addComponent(targetId, new TookDamageComponent(HIT_FLASH_DURATION));

                    // Spawn Hit Particles
                    int hitX = targetPos.x + targetCollider.offsetX + targetCollider.hitboxWidth / 2;
                    int hitY = targetPos.y + targetCollider.offsetY + targetCollider.hitboxHeight / 2;
                    int particleCount = 7 + random.nextInt(6);

                    spawnHitParticles(hitX, hitY, attackerPos.x, attackerPos.y, particleCount, 45);
                    //right after you confirmed a successful hit
                    // spawnDirectionalParticles(attackerPos.x, attackerPos.y, hitX,   hitY, 20, 0xFFFF3030);   // 20 red flecks
                    // Ensure target has the component (add it if missing)
                    ParticipantComponent participants = entityManager.getComponent(targetId, ParticipantComponent.class);
                    if (participants == null) {
                        participants = new ParticipantComponent();
                        entityManager.addComponent(targetId, participants);
                    }
                    // Only add players as participants for XP distribution? Or any attacker? Add player for now.
                    if (attackerIsPlayer && damageDealt > 0) {
                        participants.recordDamage(attackerId, damageDealt);
                        log.trace("Player {} dealt {} damage to entity {}. Total recorded: {}", attackerId, damageDealt, targetId, participants.getDamageDealtBy(attackerId));
                    }

                    // --- Check for death ---
                    if (targetHealth.currentHealth <= 0 && !entityManager.hasComponent(targetId, DeadComponent.class)) {
                        log.info("Entity {} died.", targetId);

                        // <<< Add DefeatedWithParticipantsComponent >>>
                        XPValueComponent xpValComp = entityManager.getComponent(targetId, XPValueComponent.class);
                        ParticipantComponent finalParticipants = entityManager.getComponent(targetId, ParticipantComponent.class);
                        if (xpValComp != null && finalParticipants != null && !finalParticipants.isEmpty()) {
                            // Pass the XP value and a copy of the participant set
                            entityManager.addComponent(targetId, new DefeatedWithParticipantsComponent(xpValComp.xpValue, finalParticipants.getDamageMap()));
                            log.debug("Added DefeatedWithParticipantsComponent to entity {} with {} participants.", targetId, finalParticipants.getParticipantCount());
                        } else {
                            log.debug("Entity {} died but had no XP value or no participants.", targetId);
                        }
                        // <<< End Defeated Component >>>

                        // Add DeadComponent (defaults stage to 0)
                        entityManager.addComponent(targetId, new DeadComponent());

                        // Remove interaction components
                        entityManager.removeComponent(targetId, ColliderComponent.class);
                        entityManager.removeComponent(targetId, WanderAIComponent.class); // Or other AI
                        entityManager.removeComponent(targetId, AttackComponent.class);
                        entityManager.removeComponent(targetId, AttackActionComponent.class);
                        entityManager.removeComponent(targetId, TookDamageComponent.class);
                        // entityManager.removeComponent(targetId, ParticipantComponent.class);
                        // Optional: Stop movement if dead
                        if (entityManager.hasComponent(targetId, VelocityComponent.class)) {
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

    private void spawnHitParticles(
            int hitX, int hitY,
            int attackerX, int attackerY,
            int count,
            float spreadDegrees) {
        float spread = (float) Math.toRadians(spreadDegrees);
        float sigma = (float) Math.toRadians(spreadDegrees);      // 95 % of particles inside ±20 °
        float angleOffset = (float) (random.nextGaussian() * sigma);

        int particleColor = 0xFFFF2222;                 // reddish
        float lifetime = 0.3f + random.nextFloat() * 0.4f;

        float minPxPerTick = 0.5f;  //  ≈30 px / s
        float maxPxPerTick = 2.0f;  // ≈120 px / s

        // -------- base direction away from attacker --------
        float dx = hitX - attackerX;
        float dy = hitY - attackerY;
        float len = (float) Math.hypot(dx, dy);
        if (len == 0) {
            dx = 1;
            dy = 0;
            len = 1;
        }      // attacker standing on target
        float baseX = dx / len;
        float baseY = dy / len;

        for (int i = 0; i < count; i++) {
            int e = entityManager.createEntity();

            // position ✱slightly✱ jittered around the hit point
            entityManager.addComponent(
                    e, new PositionComponent(hitX + random.nextInt(5) - 2,
                            hitY + random.nextInt(5) - 2));

            // ----- pick a direction inside the cone -----
            float a = (random.nextFloat() - .5f) * angleOffset;
            float cos = (float) Math.cos(a);
            float sin = (float) Math.sin(a);
            float dirX = baseX * cos - baseY * sin;
            float dirY = baseX * sin + baseY * cos;

            // ----- speed is *per-tick* -----
            float speed = minPxPerTick +
                    random.nextFloat() * (maxPxPerTick - minPxPerTick);

            int vx = Math.round(dirX * speed);
            int vy = Math.round(dirY * speed);
            // guarantee at least 1 px so the particle moves
            if (vx == 0 && vy == 0) {
                if (Math.abs(dirX) > Math.abs(dirY)) vx = dirX > 0 ? 1 : -1;
                else vy = dirY > 0 ? 1 : -1;
            }

            entityManager.addComponent(e, new VelocityComponent(vx, vy));

            // visuals & lifetime
            int size = 1 + random.nextInt(3);
            entityManager.addComponent(e, new ParticleComponent(particleColor, size));
            entityManager.addComponent(e, new LifetimeComponent(lifetime));

            // simple Z-physics (optional small upward pop)
            ParticlePhysicsComponent phys = new ParticlePhysicsComponent();
            phys.z = 2f + random.nextFloat() * 2f;
            phys.vz = 60f;           // 1 px per tick upward
            entityManager.addComponent(e, phys);
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
