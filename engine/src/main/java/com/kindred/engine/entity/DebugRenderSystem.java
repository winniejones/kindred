package com.kindred.engine.entity;

import com.kindred.engine.level.Level;
import com.kindred.engine.render.Screen;

public class DebugRenderSystem {

    private final EntityManager entityManager;
    private final Screen screen;
    private final Level level;

    public DebugRenderSystem(EntityManager entityManager, Screen screen, Level level) {
        this.entityManager = entityManager;
        this.screen = screen;
        this.level = level;
    }

    public void render() {
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, ColliderComponent.class)) {
            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            ColliderComponent col = entityManager.getComponent(entity, ColliderComponent.class);
            int hitboxX = col.getHitboxX(pos); // pos.x + col.offsetX
            int hitboxY = col.getHitboxY(pos); // pos.y + col.offsetY

            if (pos != null && col != null) {
                screen.drawRect(hitboxX, hitboxY, col.hitboxWidth, col.hitboxHeight, 0xFFFF00, true);
                screen.fillRect(pos.x - 1, pos.y - 1, 3, 3, 0xFF0000, true);
            }
        }

        int camX = screen.xOffset;
        int camY = screen.yOffset;
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                if (level.isSolid(x, y)) {
                    screen.drawRect(x * level.getTileSize() - camX, y * level.getTileSize() - camY,
                            level.getTileSize(), level.getTileSize(), 0x99FF0000, false);
                }
            }
        }
    }
}
