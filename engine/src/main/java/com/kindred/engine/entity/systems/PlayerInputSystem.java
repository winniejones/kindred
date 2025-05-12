package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.input.Keyboard;
import com.kindred.engine.resource.AnimationDataRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class PlayerInputSystem implements System {
    private final EntityManager entityManager;
    private final Keyboard keyboard;
    private int playerEntity = -1;
    private final AnimationDataRegistry animationRegistry;

    public PlayerInputSystem(EntityManager entityManager, Keyboard keyboard, AnimationDataRegistry animationRegistry) {
        this.entityManager = entityManager;
        this.keyboard = keyboard;
        this.animationRegistry = animationRegistry;
        log.info("PlayerInputSystem initialized.");
    }

    public void update(float deltaTime) {
        if (playerEntity == -1 || !entityManager.isEntityActive(playerEntity)) {
            Integer foundPlayer = entityManager.getFirstEntityWith(PlayerComponent.class);
            if (foundPlayer != null) {
                this.playerEntity = foundPlayer;
                log.info("PlayerInputSystem found player entity with ID: {}", this.playerEntity);
            } else {
                return;
            }
        }

        if (!entityManager.hasComponent(playerEntity, VelocityComponent.class) ||
            !entityManager.hasComponent(playerEntity, AttackComponent.class) ||
            !entityManager.hasComponent(playerEntity, AnimationComponent.class))
        {
            log.error("Player entity {} is missing essential components (Velocity, Attack, or Animation).", playerEntity);
            return;
        }

        VelocityComponent vel = entityManager.getComponent(playerEntity, VelocityComponent.class);
        AttackComponent attackComp = entityManager.getComponent(playerEntity, AttackComponent.class);
        AnimationComponent animComp = entityManager.getComponent(playerEntity, AnimationComponent.class);

        // --- Movement Input (Only if not currently attacking) ---
        if (!animComp.isAttacking) {
            vel.vx = 0;
            vel.vy = 0;
            int speed = 2;
            if (keyboard.up) vel.vy = -speed;
            if (keyboard.down) vel.vy = speed;
            if (keyboard.left) vel.vx = -speed;
            if (keyboard.right) vel.vx = speed;

            // Update direction in AnimationComponent based on movement
            // This logic should ideally be in AnimationSystem or handled by setDirection carefully
            if (vel.vx != 0 || vel.vy != 0) {
                int newDirection = animComp.direction;
                if (vel.vy < 0) newDirection = AnimationComponent.UP;
                else if (vel.vy > 0) newDirection = AnimationComponent.DOWN;
                else if (vel.vx < 0) newDirection = AnimationComponent.LEFT;
                else if (vel.vx > 0) newDirection = AnimationComponent.RIGHT;
                animComp.setDirection(newDirection);
            }
        } else {
            // If attacking, typically freeze movement or allow slow movement
            vel.vx = 0;
            vel.vy = 0;
        }
        // --- End Movement Input ---

        // --- Attack Input ---
        if (keyboard.space) { // Assuming space is the attack key
            if (!animComp.isAttacking && attackComp.currentCooldown <= 0) {
                log.debug("Player {} attempts attack!", playerEntity);

                animComp.isAttacking = true; // Set the character's state to attacking

                String weaponType = "GENERIC_SLASH"; // Placeholder

                BufferedImage[][] allAttackEffectFrames = animationRegistry.getAttackAnimationFrames(weaponType, animComp.direction);
                Map<Integer, List<Rectangle>> hitboxesForEffect = animationRegistry.getAttackHitboxes(weaponType, animComp.direction);
                float frameDuration = animationRegistry.getAttackFrameDuration(weaponType);
                int totalFramesInSequence = animationRegistry.getNumberOfAttackFrames(weaponType);

                if (allAttackEffectFrames != null && animComp.direction < allAttackEffectFrames.length && allAttackEffectFrames[animComp.direction] != null) {
                    // Create and add the AttackVisualEffectComponent
                    AttackVisualEffectComponent effectComp = new AttackVisualEffectComponent(
                        allAttackEffectFrames[animComp.direction], // Pass only the frames for the current direction
                        hitboxesForEffect,
                        frameDuration,
                        totalFramesInSequence,
                        animComp.direction // Pass current direction to the effect
                    );
                    entityManager.addComponent(playerEntity, effectComp);

                    entityManager.addComponent(playerEntity, new AttackActionComponent());

                    AttackingStateComponent attackingState = entityManager.getComponent(playerEntity, AttackingStateComponent.class);
                    if (attackingState == null) {
                        attackingState = new AttackingStateComponent();
                        entityManager.addComponent(playerEntity, attackingState);
                    }
                    attackingState.clearHitTargets();

                    attackComp.currentCooldown = attackComp.attackCooldown;
                    log.debug("Player {} started attack. Added AttackVisualEffectComponent.", playerEntity);
                } else {
                    log.warn("Could not retrieve attack EFFECT frames for weapon: {} and direction: {}. Attack visual not initiated.", weaponType, animComp.direction);
                    animComp.isAttacking = false; // Revert state if effect can't be created
                }
            }
        }

        // --- Interaction Input ---
        if (keyboard.interact && !animComp.isAttacking) { // Prevent interaction while attacking
            if (!entityManager.hasComponent(playerEntity, InteractionAttemptComponent.class)) {
                log.debug("Player {} initiates interaction attempt!", playerEntity);
                entityManager.addComponent(playerEntity, new InteractionAttemptComponent());
            }
        }
    }
}
