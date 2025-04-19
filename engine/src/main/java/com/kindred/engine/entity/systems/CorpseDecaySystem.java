package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.resource.AssetLoader;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CorpseDecaySystem implements System {
    private final EntityManager entityManager;

    // --- Sprite Cache for Decay Stages (Made Static) ---
    // Maps Type Key (String) -> List of decay stage BufferedImages
    private static final Map<String, List<BufferedImage>> decaySprites = new HashMap<>();
    private static final int NUM_DECAY_STAGES = 3; // Example: 0=Fresh, 1=Decayed, 2=Bones
    private static boolean spritesLoaded = false; // Flag to ensure loading happens once

    // --- Static Initializer Block to Load Sprites ---
    static {
        loadAllDecaySprites();
    }

    public CorpseDecaySystem(EntityManager entityManager) {
        this.entityManager = entityManager;
        if (!spritesLoaded) { // Fallback if static block failed somehow
            log.warn("Attempting to load decay sprites in constructor (should have happened in static block).");
            loadAllDecaySprites();
        }
        log.info("CorpseDecaySystem initialized.");
    }

    // --- Load and Cache Decay Sprites ---
    private static void loadAllDecaySprites() {
        if (spritesLoaded) return; // Prevent reloading
        log.debug("Loading all decay sprites...");
        // Example for Deidara
        loadSprite("ENEMY_DEIDARA", "/assets/sprites/decaying_deidara_corpse.png", NUM_DECAY_STAGES);
        // Add other entity types here
        spritesLoaded = true; // Mark as loaded
    }

    private static void loadSpriteSequence(String typeKey, String basePath, int numStages) {
        List<BufferedImage> sprites = new ArrayList<>();
        log.debug("Loading decay sprites for type: {}", typeKey);
        for (int i = 0; i < numStages; i++) {
            String path = basePath + i + ".png"; // Assumes naming convention like slime_corpse_stage0.png
            BufferedImage sprite = AssetLoader.loadImage(path);
            // Use placeholder if load fails, ensuring list size matches numStages
            if (sprite == null || sprite.getWidth() <= 1) {
                log.warn("Failed to load decay sprite: {}. Using placeholder.", path);
                sprite = AssetLoader.createPlaceholderImage(32, 32); // Use AssetLoader's placeholder
            }
            sprites.add(sprite);
        }
        if (!sprites.isEmpty()) {
            decaySprites.put(typeKey, sprites);
        } else {
            log.error("Failed to load any decay sprites for type: {}", typeKey);
        }
    }

    private static void loadSprite(String typeKey, String path, int numStages) {
        List<BufferedImage> sprites = new ArrayList<>();
        log.debug("Loading decay sprites for type: {}", typeKey);
        BufferedImage sheet = AssetLoader.loadImage(path);

        if (sheet == null || sheet.getWidth() <= 1 || sheet.getHeight() <= 1) {
            log.error("AssetLoader Error: Failed to load sheet or sheet is placeholder for animation: {}", path);
        }

        for (int i = 0; i < numStages; i++) {
            // Use placeholder if load fails, ensuring list size matches numStages
            BufferedImage frame;
            if (sheet == null || sheet.getWidth() <= 1) {
                log.warn("Failed to load decay sprite: {}. Using placeholder.", path);
                frame = AssetLoader.createPlaceholderImage(32, 32); // Use AssetLoader's placeholder
            }
            frame = AssetLoader.getSprite(sheet, i, 0, 32, 32);
            sprites.add(frame);
        }
        if (!sprites.isEmpty()) {
            decaySprites.put(typeKey, sprites);
        } else {
            log.error("Failed to load any decay sprites for type: {}", typeKey);
        }
    }

    /**
     * Static method to get a specific decay sprite.
     * Can be called by other systems (like CombatSystem) to get initial corpse sprite.
     * @param typeKey String identifying the entity type (e.g., "ENEMY_SLIME").
     * @param stage The decay stage index (0-based).
     * @return The corresponding BufferedImage, or a placeholder if not found.
     */
    public static BufferedImage getDecaySprite(String typeKey, int stage) {
        List<BufferedImage> sprites = decaySprites.get(typeKey);
        if (sprites != null && stage >= 0 && stage < sprites.size()) {
            BufferedImage sprite = sprites.get(stage);
            // Return placeholder if the specific sprite failed loading earlier
            return (sprite != null && sprite.getWidth() > 1) ? sprite : AssetLoader.createPlaceholderImage(32, 32);
        }
        log.warn("Could not find decay sprite for type '{}', stage {}", typeKey, stage);
        return AssetLoader.createPlaceholderImage(32, 32); // Fallback placeholder
    }
    // --- End Sprite Loading ---


    @Override
    public void update(float deltaTime) {
        // Query for entities that are dead and have a lifetime (i.e., are corpses)
        for (int entity : entityManager.getEntitiesWith(
                DeadComponent.class,
                LifetimeComponent.class,
                SpriteComponent.class // Need sprite component to update it
                // PositionComponent is not strictly needed by this system's logic
        )) {
            if (!entityManager.isEntityActive(entity)) continue; // Check if entity still exists

            DeadComponent deadComp = entityManager.getComponent(entity, DeadComponent.class);
            LifetimeComponent lifetime = entityManager.getComponent(entity, LifetimeComponent.class);
            SpriteComponent spriteComp = entityManager.getComponent(entity, SpriteComponent.class);

            if (deadComp == null || lifetime == null || spriteComp == null || lifetime.initialLifetime <= 0) continue; // Safeguard

            // Calculate decay progress
            float progress = 1.0f - (lifetime.remainingLifetime / lifetime.initialLifetime);
            progress = Math.max(0.0f, Math.min(1.0f, progress));

            // Determine the required visual stage based on progress
            // Example: 3 stages (0, 1, 2). Stage 0 up to 33%, Stage 1 up to 66%, Stage 2 after.
            int requiredStage = (int) (progress * NUM_DECAY_STAGES);
            // Ensure stage index stays within bounds (e.g., if progress is exactly 1.0)
            requiredStage = Math.min(requiredStage, NUM_DECAY_STAGES - 1);

            // If the required stage is different from the current stage, update the sprite
            if (requiredStage != deadComp.decayStage) {
                log.trace("Entity {} decaying to stage {}", entity, requiredStage);

                // Determine entity type to get correct sprite set
                String typeKey = "UNKNOWN"; // Default key
                if (entityManager.hasComponent(entity, EnemyComponent.class)) { // Crude type check
                    typeKey = "ENEMY_DEIDARA"; // TODO: Need better type identification if multiple enemies
                }
                /*
                *
                else if (entityManager.hasComponent(entity, NPCComponent.class)) {
                    typeKey = "NPC_VILLAGER"; // TODO: Need better type identification
                }
                */
                // Add more type checks as needed

                // Get the new sprite for the required stage
                BufferedImage newSprite = getDecaySprite(typeKey, requiredStage);

                // Update the entity's sprite component
                spriteComp.sprite = newSprite;

                // Update the stored decay stage
                deadComp.decayStage = requiredStage;
            }
        }
    }
}
