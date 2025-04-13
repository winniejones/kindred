package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.PlayerComponent;
import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.level.Level;
import com.kindred.engine.render.Screen;

public class CameraSystem implements System {
    private final EntityManager entityManager;
    private final Screen screen;
    private final Level level;

    /**
     * Constructor for CameraSystem.
     * @param entityManager The EntityManager to find entities.
     * @param screen The Screen object to update the rendering offset.
     * @param level The Level object to get boundaries for clamping.
     */
    public CameraSystem(EntityManager entityManager, Screen screen, Level level) {
        // Null checks for required dependencies
        if (entityManager == null) throw new IllegalArgumentException("EntityManager cannot be null.");
        if (screen == null) throw new IllegalArgumentException("Screen cannot be null.");
        if (level == null) throw new IllegalArgumentException("Level cannot be null.");

        this.entityManager = entityManager;
        this.screen = screen;
        this.level = level;
    }

    @Override
    public void update(float deltaTime) {
        Integer playerEntity = entityManager.getFirstEntityWith(PlayerComponent.class, PositionComponent.class);

        if (playerEntity == null) return;

        PositionComponent playerPos = entityManager.getComponent(playerEntity, PositionComponent.class);

        // --- Calculate desired camera position (center player) ---
        // Note: playerPos.x/y is the top-left of the player entity by default.
        // If you want to center based on the player's visual center, add half player size.
        int PLAYER_SPRITE_WIDTH = 32;
        int playerCenterX = playerPos.x + PLAYER_SPRITE_WIDTH / 2;
        int PLAYER_SPRITE_HEIGHT = 32;
        int playerCenterY = playerPos.y + PLAYER_SPRITE_HEIGHT / 2;
        int camX = playerCenterX - screen.width / 2;
        int camY = playerCenterY - screen.height / 2;


        // --- Clamp camera to level boundaries ---
        int levelPixelWidth = level.getWidth() * level.getTileSize();
        int levelPixelHeight = level.getHeight() * level.getTileSize();

        // Calculate max camera offset (top-left corner)
        int maxCamX = Math.max(0, levelPixelWidth - screen.width); // Ensure not negative if level smaller than screen
        int maxCamY = Math.max(0, levelPixelHeight - screen.height);

        // Apply clamping
        camX = Math.max(0, Math.min(camX, maxCamX));
        camY = Math.max(0, Math.min(camY, maxCamY));


        // --- Update Screen Offset ---
        // Update the screen directly (or update CameraComponent if used)
        screen.setOffset(camX, camY);

        // --- Optional: Update CameraComponent ---
        // if (camera != null) {
        //     camera.x = camX;
        //     camera.y = camY;
        // }

        // --- Debug Print (Optional) ---
        // System.out.print("Camera Offset: (" + camX + ", " + camY + ")");
        // System.out.println("\tPlayer Pos: (" + playerPos.x + ", " + playerPos.y + ")");
    }
}
