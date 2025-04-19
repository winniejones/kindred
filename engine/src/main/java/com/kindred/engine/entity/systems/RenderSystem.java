package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.render.Screen;

// Import SLF4J/Lombok if using logging
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;

@Slf4j
public class RenderSystem implements System {

    private final EntityManager entityManager;
    private final Screen screen;

    public RenderSystem(EntityManager entityManager, Screen screen) {
        this.entityManager = entityManager;
        this.screen = screen;
        log.info("RenderSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        render();
    }

    public void render() {
        // --- Render Standard Sprites ---
        renderSprites();

        // --- Render Particles ---
        renderPractice();
        // --- End Render Particles ---

    } // End render()

    private void renderPractice() {
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, ParticleComponent.class)) {
            // Dead particles are removed by LifetimeSystem, no need to check DeadComponent
            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            ParticleComponent particle = entityManager.getComponent(entity, ParticleComponent.class);
            ParticlePhysicsComponent physics = entityManager.getComponent(entity, ParticlePhysicsComponent.class); // Get physics component

            if (pos == null || particle == null || physics == null) continue;

            // Calculate screen Y position by subtracting Z height
            int screenY = pos.y - (int)physics.z; // Adjust offset as needed
            int screenX = pos.x - particle.size / 2; // Center particle

            // Draw particle as a simple colored rectangle using fillRect
            screen.fillRect(screenX, screenY, particle.size, particle.size, particle.color, true); // true = use offset
        }
    }

    private void renderSprites() {
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, SpriteComponent.class)) {
            // Skip rendering particles here, handle them separately
            if (entityManager.hasComponent(entity, ParticleComponent.class)) {
                continue;
            }

            // We render entities with DeadComponent (corpses)

            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            SpriteComponent spriteComp = entityManager.getComponent(entity, SpriteComponent.class);
            if (pos == null || spriteComp == null || spriteComp.sprite == null) continue;

            BufferedImage currentSprite = spriteComp.sprite;
            boolean applyFlash = false;

            if (!entityManager.hasComponent(entity, DeadComponent.class) &&
                    entityManager.hasComponent(entity, TookDamageComponent.class))
            {
                TookDamageComponent flash = entityManager.getComponent(entity, TookDamageComponent.class);
                int flickerSegments = (int) (flash.effectTimer / 0.05f);
                if (flickerSegments % 2 == 0) {
                    applyFlash = true;
                }
            }

            if (!applyFlash) {
                // Draw standard sprite at its base X, Y position
                screen.drawSpriteWithAlpha(pos.x, pos.y, currentSprite);
            }
        } // End standard sprite loop
    }
}
