package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.ColliderComponent;
import com.kindred.engine.entity.components.DeadComponent;
import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.level.Level;
import com.kindred.engine.render.Screen;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DebugRenderSystem {

    private final EntityManager entityManager;
    private final Screen screen;
    private final Level level; // Optional: for drawing tile grid etc.
    private boolean drawHitboxes = true; // Toggle to enable/disable hitbox drawing
    private boolean drawTileGrid = false; // Toggle for grid

    public DebugRenderSystem(EntityManager entityManager, Screen screen, Level level) {
        this.entityManager = entityManager;
        this.screen = screen;
        this.level = level;
        log.info("DebugRenderSystem initialized.");
    }

    public void render() {
        if (drawHitboxes) {
            renderHitboxes();
        }
        if (drawTileGrid) {
            renderTileGrid();
        }
    }
    private void renderHitboxes() {
        // Query for entities with position and collider
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, ColliderComponent.class)) {

            // <<< Add Check for DeadComponent >>>
            // Skip drawing debug info for dead entities
            if (entityManager.hasComponent(entity, DeadComponent.class)) {
                continue;
            }
            // <<< End Check >>>


            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            ColliderComponent col = entityManager.getComponent(entity, ColliderComponent.class);

            if (pos == null || col == null) continue;

            // Calculate hitbox screen coordinates (including offset and camera)
            int hitboxScreenX = pos.x + col.offsetX; // World X
            int hitboxScreenY = pos.y + col.offsetY; // World Y

            // Use screen.drawRect (assuming it handles camera offset via 'fixed=true')
            // Need to create drawRect if it doesn't exist or use fillRect carefully
            // Example using fillRect to draw an outline:
            int color = 0xFFFF0000; // Red outline example
            // screen.drawRect(hitboxScreenX, hitboxScreenY, col.hitboxWidth, col.hitboxHeight, color, true);

            // Simulate drawRect using fillRect (less efficient)
             screen.fillRect(hitboxScreenX, hitboxScreenY, col.hitboxWidth, 1, color, true); // Top
             screen.fillRect(hitboxScreenX, hitboxScreenY + col.hitboxHeight - 1, col.hitboxWidth, 1, color, true); // Bottom
             screen.fillRect(hitboxScreenX, hitboxScreenY + 1, 1, col.hitboxHeight - 2, color, true); // Left
             screen.fillRect(hitboxScreenX + col.hitboxWidth - 1, hitboxScreenY + 1, 1, col.hitboxHeight - 2, color, true); // Right

        }
    }

    private void renderTileGrid() {
        if (level == null) return;
        int tileSize = level.getTileSize();
        int gridColor = 0xFF555555; // Dark Gray

        // Calculate visible tile range based on camera (similar to Level.render)
        int x0 = screen.xOffset / tileSize;
        int x1 = (screen.xOffset + screen.width + tileSize) / tileSize;
        int y0 = screen.yOffset / tileSize;
        int y1 = (screen.yOffset + screen.height + tileSize) / tileSize;

        x0 = Math.max(0, x0);
        x1 = Math.min(level.getWidth(), x1);
        y0 = Math.max(0, y0);
        y1 = Math.min(level.getHeight(), y1);

        // Draw vertical lines
        for (int x = x0; x <= x1; x++) {
             int screenX = x * tileSize;
             // Use fillRect to draw lines
             screen.fillRect(screenX, y0 * tileSize, 1, (y1 - y0) * tileSize, gridColor, true); // fixed=true for world coords
        }
        // Draw horizontal lines
        for (int y = y0; y <= y1; y++) {
         int screenY = y * tileSize;
         screen.fillRect(x0 * tileSize, screenY, (x1 - x0) * tileSize, 1, gridColor, true);
        }
    }
    // --- Optional: Methods to toggle debug flags ---
    public void setDrawHitboxes(boolean draw) { this.drawHitboxes = draw; }
    public void toggleDrawHitboxes() { this.drawHitboxes = !this.drawHitboxes; }
    // ... similarly for other flags ...
}
