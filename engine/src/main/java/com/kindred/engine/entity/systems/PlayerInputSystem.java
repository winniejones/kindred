package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.input.Keyboard;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.KeyEvent;

@Slf4j
public class PlayerInputSystem implements System {
    private final EntityManager entityManager;
    private final Keyboard keyboard;
    private int playerEntity = -1;

    public PlayerInputSystem(EntityManager entityManager, Keyboard keyboard) {
        this.entityManager = entityManager;
        this.keyboard = keyboard;
        // DO NOT look for player entity here - it might not exist yet!
        log.info("PlayerInputSystem initialized.");
    }

    public void update(float deltaTime) {
        // --- Find Player Entity (Lazy Initialization) ---
        if (playerEntity == -1 || !entityManager.isEntityActive(playerEntity)) {
            // Try to find the player entity if we don't have a valid ID yet
            Integer foundPlayer = entityManager.getFirstEntityWith(PlayerComponent.class);
            if (foundPlayer != null) {
                this.playerEntity = foundPlayer;
                log.info("PlayerInputSystem found player entity with ID: {}", this.playerEntity);
            } else {
                // Player still not found (maybe not spawned yet?), skip update for this system
                // log.trace("PlayerInputSystem: Player entity not found this update cycle.");
                return;
            }
        }

        // Check if essential components exist (could be done once)
        if (!entityManager.hasComponent(playerEntity, VelocityComponent.class) ||
                !entityManager.hasComponent(playerEntity, AttackComponent.class)) // Need AttackComponent now
        {
            log.error("Player entity {} is missing essential components (Velocity or Attack).", playerEntity);
            return;
        }

        VelocityComponent vel = entityManager.getComponent(playerEntity, VelocityComponent.class);
        AttackComponent attack = entityManager.getComponent(playerEntity, AttackComponent.class);

        // --- Movement Input (Keep your existing logic) ---
        vel.vx = 0;
        vel.vy = 0;
        int speed = 2; // Example speed
        if (keyboard.up) vel.vy = -speed;
        if (keyboard.down) vel.vy = speed;
        if (keyboard.left) vel.vx = -speed;
        if (keyboard.right) vel.vx = speed;
        // --- End Movement Input ---

        // --- Attack Input ---
        // TODO: Choose your attack key (e.g., Space, J, K, Mouse click?)
        if (keyboard.space) { // Check if Space is pressed THIS FRAME
            // Check if attack is off cooldown AND player isn't already attacking
            if (attack.currentCooldown <= 0 && !entityManager.hasComponent(playerEntity, AttackActionComponent.class)) {
                log.debug("Player {} initiates attack!", playerEntity);
                // Add the marker component to trigger CombatSystem
                entityManager.addComponent(playerEntity, new AttackActionComponent());
                // Reset the cooldown timer
                attack.currentCooldown = attack.attackCooldown;
            } else {
                // Optional: Log if attack is on cooldown
                // if (attack.currentCooldown > 0) log.trace("Player {} attack on cooldown ({}s left)", playerEntity, attack.currentCooldown);
            }
        }

        // --- Interaction Input ---
        // <<< Added check for interaction key (e.g., 'E') >>>
        if (keyboard.interact) {
            // Check if player isn't already trying to interact
            if (!entityManager.hasComponent(playerEntity, InteractionAttemptComponent.class)) {
                log.debug("Player {} initiates interaction attempt!", playerEntity);
                entityManager.addComponent(playerEntity, new InteractionAttemptComponent());
            }
        }
        // <<< End Interaction Input >>>
    }
}
