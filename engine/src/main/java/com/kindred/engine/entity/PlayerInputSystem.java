package com.kindred.engine.entity;

import com.kindred.engine.input.Keyboard;

public class PlayerInputSystem {
    private final EntityManager entityManager;
    private final Keyboard keyboard;

    public PlayerInputSystem(EntityManager entityManager, Keyboard keyboard) {
        this.entityManager = entityManager;
        this.keyboard = keyboard;
    }

    public void update() {
        for (int entity : entityManager.getEntitiesWith(PlayerComponent.class, VelocityComponent.class)) {
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            if (vel == null) {
                System.err.println("Entity: " + entity + " is missing Velocity component!");
                continue;
            }

            // Reset velocity
            vel.vx = 0;
            vel.vy = 0;

            // Set based on current input
            if (keyboard.up) vel.vy = -2;
            if (keyboard.down) vel.vy = 2;
            if (keyboard.left) vel.vx = -2;
            if (keyboard.right) vel.vx = 2;
        }
    }
}
