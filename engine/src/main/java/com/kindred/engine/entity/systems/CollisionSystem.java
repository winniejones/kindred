package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.ColliderComponent;
import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.level.Level;

/**
 * System responsible for handling collision detection between entities
 * (with Position, Velocity, and Collider components) and the solid tiles
 * in the game Level. It adjusts the entity's velocity component if a
 * collision is detected, preventing movement into solid areas.
 */
public class CollisionSystem {
    private final EntityManager entityManager;
    private final Level level; // Reference to the level for tile collision checks

    public CollisionSystem(EntityManager entityManager, Level level) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null.");
        }
        this.entityManager = entityManager;
        this.level = level;
    }

    /**
     * Updates all relevant entities, checking for collisions and adjusting velocity.
     * This should typically be called after input handling and before the MovementSystem.
     */
    public void update() {
        int tileSize = level.getTileSize();
        if (tileSize <= 0) {
            System.err.println("CollisionSystem: Invalid tile size in Level. Skipping collision checks.");
            return; // Cannot perform checks with invalid tile size
        }

        // Iterate through all entities that have the required components for collision detection
        for (int entity : entityManager.getEntitiesWith(
                PositionComponent.class,
                VelocityComponent.class,
                ColliderComponent.class)) {

            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            ColliderComponent col = entityManager.getComponent(entity, ColliderComponent.class);

            // Get the intended velocity for this frame (usually set by input or AI systems)
            // Note: We directly modify the vel component based on collisions.
            int currentVx = vel.vx;
            int currentVy = vel.vy;

            // Store the final velocity after collision checks
            int finalVx = currentVx;
            int finalVy = currentVy;

            // --- Calculate current hitbox top-left position using offsets ---
            // Use the helper methods from ColliderComponent for clarity
            int hitboxX = col.getHitboxX(pos); // pos.x + col.offsetX
            int hitboxY = col.getHitboxY(pos); // pos.y + col.offsetY

            // --- Horizontal Collision Check ---
            if (currentVx != 0) { // Only check if trying to move horizontally
                // Check collision using the entity's specific hitbox dimensions
                if (isColliding(hitboxX, hitboxY, currentVx, 0, col.hitboxWidth, col.hitboxHeight, tileSize)) {
                    // Collision detected! Calculate maximum allowed movement (slide).
                    if (currentVx > 0) { // Moving Right
                        int leadingEdgeX = hitboxX + col.hitboxWidth - 1; // Right edge of hitbox
                        // Find the tile X-coordinate the leading edge would enter
                        int targetTileX = (leadingEdgeX + currentVx) / tileSize;
                        // Calculate the X-coordinate of the left edge of the blocking tile
                        int blockingEdgeX = targetTileX * tileSize;
                        // Calculate the maximum distance the leading edge can move
                        // We subtract 1 because the leading edge cannot occupy the same coordinate as the blocking edge.
                        finalVx = blockingEdgeX - leadingEdgeX - 1;
                        // Ensure we don't calculate negative movement if already overlapping (shouldn't happen with good checks)
                        if (finalVx < 0) finalVx = 0;
                        // System.out.println("Slide Right: Max Vx = " + finalVx); // Debug
                    } else { // Moving Left
                        // Find the tile X-coordinate the leading edge would enter
                        int targetTileX = (hitboxX + currentVx) / tileSize;
                        // Calculate the X-coordinate of the right edge of the blocking tile
                        int blockingEdgeX = (targetTileX + 1) * tileSize;
                        // Calculate the maximum distance the leading edge can move (will be negative or zero)
                        finalVx = blockingEdgeX - hitboxX;
                        // Ensure we don't calculate positive movement if already overlapping
                        if (finalVx > 0) finalVx = 0;
                        // System.out.println("Slide Left: Max Vx = " + finalVx); // Debug
                    }

                }
            }

            // --- Vertical Collision Check ---
            // IMPORTANT: Use the ORIGINAL position (pos.x, pos.y) for this check,
            // especially if sliding logic were added above which might modify pos.x.
            if (currentVy != 0) { // Only check if trying to move vertically
                // Check collision using the entity's specific hitbox dimensions
                if (isColliding(hitboxX, hitboxY, 0, currentVy, col.hitboxWidth, col.hitboxHeight, tileSize)) {
                    if (currentVy > 0) { // Moving Down
                        int leadingEdgeY = hitboxY + col.hitboxHeight - 1; // Bottom edge of hitbox
                        // Find the tile Y-coordinate the leading edge would enter
                        int targetTileY = (leadingEdgeY + currentVy) / tileSize;
                        // Calculate the Y-coordinate of the top edge of the blocking tile
                        int blockingEdgeY = targetTileY * tileSize;
                        // Calculate the maximum distance the leading edge can move
                        finalVy = blockingEdgeY - leadingEdgeY - 1;
                        if (finalVy < 0) finalVy = 0;
                        // System.out.println("Slide Down: Max Vy = " + finalVy); // Debug
                    } else { // Moving Up
                        // Find the tile Y-coordinate the leading edge would enter
                        int targetTileY = (hitboxY + currentVy) / tileSize;
                        // Calculate the Y-coordinate of the bottom edge of the blocking tile
                        int blockingEdgeY = (targetTileY + 1) * tileSize;
                        // Calculate the maximum distance the leading edge can move
                        finalVy = blockingEdgeY - hitboxY;
                        if (finalVy > 0) finalVy = 0;
                        // System.out.println("Slide Up: Max Vy = " + finalVy); // Debug
                    }
                }
            }

            // Update the entity's velocity component with the final, collision-adjusted values
            vel.vx = finalVx;
            vel.vy = finalVy;
        }
    }

    /**
     * Checks for collision between a hitbox at a potential future position and solid tiles.
     * Assumes (x, y) is the top-left corner of the hitbox.
     *
     * @param x            Current X position (top-left of hitbox).
     * @param y            Current Y position (top-left of hitbox).
     * @param xa           Potential horizontal movement offset for this check.
     * @param ya           Potential vertical movement offset for this check.
     * @param hitboxWidth  The width of the collision hitbox.
     * @param hitboxHeight The height of the collision hitbox.
     * @param tileSize     The size (width/height) of a single map tile.
     * @return True if a collision is detected, false otherwise.
     */
    private boolean isColliding(int x, int y, int xa, int ya, int hitboxWidth, int hitboxHeight, int tileSize) {
        // Check all 4 corners of the hitbox's potential future position
        for (int c = 0; c < 4; c++) {
            // Calculate corner offsets relative to the hitbox top-left
            // Use (width - 1) and (height - 1) to get the coordinates of the far edges.
            int cornerXOffset = (c % 2) * (hitboxWidth - 1);
            int cornerYOffset = (c / 2) * (hitboxHeight - 1);

            // Calculate the absolute world coordinates of the corner *after* the potential move
            int futureCornerX = x + xa + cornerXOffset;
            int futureCornerY = y + ya + cornerYOffset;

            // Convert the world coordinates of the corner to tile coordinates
            int tileX = futureCornerX / tileSize;
            int tileY = futureCornerY / tileSize;

            // Check if the tile at these coordinates is solid using the Level object
            if (level.isSolid(tileX, tileY)) {
                // System.out.println("Collision Check: Corner " + c + " at world (" + futureCornerX + "," + futureCornerY + ") -> tile (" + tileX + "," + tileY + ") is Solid."); // Detailed debug
                return true; // Collision detected
            }
        }
        // No collision detected at any of the four corners
        return false;
    }
}
