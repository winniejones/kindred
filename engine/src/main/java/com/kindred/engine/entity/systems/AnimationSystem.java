package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AnimationSystem implements System {

    private final EntityManager entityManager;

    public AnimationSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
        log.info("AnimationSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        for (int entity : List.copyOf(entityManager.getEntitiesWith(
                AttackVisualEffectComponent.class,
                AnimationComponent.class
        ))) {
            AttackVisualEffectComponent effectComp = entityManager.getComponent(entity, AttackVisualEffectComponent.class);
            AnimationComponent animComp = entityManager.getComponent(entity, AnimationComponent.class); // Still need this for isAttacking state

            if (effectComp == null || animComp == null) continue; // Should not happen if query is correct

            // Ensure the entity is still meant to be in an "attacking" state.
            // This is a safeguard; isAttacking should primarily be managed by input/AI and this system.
            if (!animComp.isAttacking) {
                log.warn("Entity {} has AttackVisualEffectComponent but AnimationComponent.isAttacking is false. Cleaning up effect.", entity);
                entityManager.removeComponent(entity, AttackVisualEffectComponent.class);
                entityManager.removeComponent(entity, AttackActionComponent.class);
                entityManager.removeComponent(entity, AttackingStateComponent.class);
                continue;
            }

            if (effectComp.frames == null || effectComp.frames.length == 0 || effectComp.frameDuration <= 0) {
                log.warn("Entity {}: Invalid AttackVisualEffectComponent data. Removing effect.", entity);
                entityManager.removeComponent(entity, AttackVisualEffectComponent.class);
                entityManager.removeComponent(entity, AttackActionComponent.class);
                entityManager.removeComponent(entity, AttackingStateComponent.class);
                animComp.isAttacking = false;
                continue;
            }

            effectComp.animationTimer += deltaTime;
            if (effectComp.animationTimer >= effectComp.frameDuration) {
                effectComp.animationTimer -= effectComp.frameDuration;
                effectComp.currentFrame++;

                if (effectComp.currentFrame >= effectComp.totalFrames) {
                    // Attack visual effect animation finished
                    log.trace("Entity {} finished attack visual effect.", entity);
                    entityManager.removeComponent(entity, AttackVisualEffectComponent.class);
                    entityManager.removeComponent(entity, AttackActionComponent.class); // For CombatSystem
                    entityManager.removeComponent(entity, AttackingStateComponent.class); // For hit-once logic
                    animComp.isAttacking = false; // Reset the character's main attack state
                }
            }
        }

        // --- Second, update base character walk/idle animations ---
        for (int entity : entityManager.getEntitiesWith(
                AnimationComponent.class,
                SpriteComponent.class
        )) {
            AnimationComponent animComp = entityManager.getComponent(entity, AnimationComponent.class);
            SpriteComponent spriteComp = entityManager.getComponent(entity, SpriteComponent.class);

            if (spriteComp == null || animComp == null) continue;

            // +++ MODIFIED LOGIC FOR WALK/IDLE AND CORPSE SPRITE HANDLING +++
            if (entityManager.hasComponent(entity, DeadComponent.class)) {
                // If entity is dead, do NOT update its sprite from AnimationComponent.
                // CorpseDecaySystem or CombatSystem should handle the sprite for dead entities.
                continue;
            }

            if (!animComp.isAttacking) {
                VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
                boolean moving = (vel != null && (vel.vx != 0 || vel.vy != 0));

                if (moving) {
                    animComp.update();
                } else {
                    // Entity is not moving and not attacking, reset to idle pose (frame 0)
                    animComp.frame = 0;
                    animComp.tick = 0;
                }
                spriteComp.sprite = animComp.getCurrentFrame();
            } else {
                // Entity is in the "isAttacking" state.
                // Its base sprite (character body) should ideally be an attack stance/pose or freeze.
                // The animComp.update() is currently designed to 'return' if isAttacking is true,
                // so it freezes the last walk/idle frame. This is acceptable for now.
                // If you want a specific attack pose, AnimationComponent.update() would need
                // to set animComp.frame to that pose's frame index when isAttacking is true.
                // The SpriteComponent.sprite is already showing this frozen/pose frame.
                // The actual attack *effect* is handled by AttackVisualEffectComponent and RenderSystem.
            }
        }
    }
}
