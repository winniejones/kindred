package com.kindred.engine.entity;

import com.kindred.engine.render.Screen;

public class RenderSystem {
    private final EntityManager entityManager;
    private final Screen screen;

    public RenderSystem(EntityManager entityManager, Screen screen) {
        this.entityManager = entityManager;
        this.screen = screen;
    }
    public void render() {
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, SpriteComponent.class)) {
            //screen.fillRect(pos.x, pos.y, 64, 64, 0xff00ff00, false); // green rectangle
            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            SpriteComponent sprite = entityManager.getComponent(entity, SpriteComponent.class);
            screen.drawSpriteWithColorKey(pos.x, pos.y, sprite.sprite, 0xffff00ff);
        }
    }
}
