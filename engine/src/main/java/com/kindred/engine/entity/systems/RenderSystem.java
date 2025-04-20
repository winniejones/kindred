package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.render.Screen;

// Import SLF4J/Lombok if using logging
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.*;

@Slf4j
public class RenderSystem implements System {

    private final EntityManager entityManager;
    private final Screen screen;

    // List to hold entities for sorting - reused to avoid reallocation
    private final List<Integer> entitiesToRender = new ArrayList<>();

    // --- Custom Comparator for Rendering Order ---
    private final Comparator<Integer> renderOrderComparator;

    public RenderSystem(EntityManager entityManager, Screen screen) {
        // 1. Assign final fields FIRST
        this.entityManager = entityManager;
        this.screen = screen;
        // 2. Initialize the comparator AFTER entityManager is assigned
        this.renderOrderComparator = getRenderOrderComparator(entityManager);
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
        // --- 1. Gather Renderable Entities ---
        prepareEntityList();

        // --- 2. Sort Entities using Custom Comparator ---
        entitiesToRender.sort(renderOrderComparator);

        for (int entity : entitiesToRender) {
            // Skip rendering particles here, handle them separately
            if (!entityManager.isEntityActive(entity)) {
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



    private Comparator<Integer> getRenderOrderComparator(EntityManager entityManager) {
        return (entityA, entityB) -> {
            // Get components safely, handle potential nulls if entities were removed between cycles
            PositionComponent posA = entityManager.getComponent(entityA, PositionComponent.class);
            PositionComponent posB = entityManager.getComponent(entityB, PositionComponent.class);
            boolean isADead = entityManager.hasComponent(entityA, DeadComponent.class);
            boolean isBDead = entityManager.hasComponent(entityB, DeadComponent.class);

            // --- Layering Logic ---
            // 1. If one is dead and the other isn't, dead comes first (-1)
            if (isADead && !isBDead) {
                return -1; // A (dead) comes before B (alive)
            }
            if (!isADead && isBDead) {
                return 1;  // A (alive) comes after B (dead)
            }

            // --- Y-Sorting within Layers ---
            // 2. If both are dead or both are alive, sort by Y (bottom edge)
            // Handle cases where position might be null (shouldn't happen for renderables, but safe)
            int yA = (posA != null) ? getYBottom(posA, entityA) : Integer.MAX_VALUE;
            int yB = (posB != null) ? getYBottom(posB, entityB) : Integer.MAX_VALUE;

            return Integer.compare(yA, yB);
        };
    }

    // Helper to get the bottom Y coordinate for sorting
    private int getYBottom(PositionComponent pos, int entityId) {
        int height = 32; // Default height
        // Try getting height from sprite first
        SpriteComponent sc = entityManager.getComponent(entityId, SpriteComponent.class);
        if (sc != null && sc.sprite != null) {
            height = sc.sprite.getHeight();
        } else {
            // Fallback to collider height if no sprite?
            ColliderComponent col = entityManager.getComponent(entityId, ColliderComponent.class);
            if (col != null) {
                // Use collider bottom edge relative to position origin
                return pos.y + col.offsetY + col.hitboxHeight;
            }
        }
        // Default sort position if using only pos.y + sprite height
        return pos.y + height;
    }
    // --- End Custom Comparator ---

    private void prepareEntityList() {
        entitiesToRender.clear();
        Set<Integer> initialRenderableSet = entityManager.getEntitiesWith(PositionComponent.class, SpriteComponent.class);
        Set<Integer> renderableSet = new HashSet<>(initialRenderableSet);
        renderableSet.removeIf(entityId -> entityManager.hasComponent(entityId, ParticleComponent.class));
        entitiesToRender.addAll(renderableSet);
    }
}
