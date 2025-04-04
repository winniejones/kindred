package com.kindred.engine.entity;

import com.kindred.engine.render.Screen;

public class CameraSystem {

    private final EntityManager entityManager;
    private final Screen screen;

    /*
     * Test
     * */
    private final int tileSize = 32;
    private final int mapWidth = 50;
    private final int mapHeight = 30;

    public CameraSystem(EntityManager entityManager, Screen screen) {
        this.entityManager = entityManager;
        this.screen = screen;
    }

    public void update() {
        Integer cameraEntity = entityManager.getFirstEntityWith(CameraComponent.class);
        Integer playerEntity = entityManager.getFirstEntityWith(PlayerComponent.class, PositionComponent.class);

        if (cameraEntity == null || playerEntity == null) return;

        CameraComponent camera = entityManager.getComponent(cameraEntity, CameraComponent.class);
        PositionComponent playerPos = entityManager.getComponent(playerEntity, PositionComponent.class);

        int camX = playerPos.x - screen.width / 2;
        int camY = playerPos.y - screen.height / 2;

        // Clamp to map bounds
        camX = Math.max(0, Math.min(camX, tileSize * mapWidth - screen.width));
        camY = Math.max(0, Math.min(camY, tileSize * mapHeight - screen.height));

        camera.x = camX;
        camera.y = camY;

        screen.setOffset(camera.x, camera.y);

        System.out.println("Camera: (" + camX + ", " + camY + ")");
        System.out.println("Player: (" + playerPos.x + ", " + playerPos.y + ")");
        return; // assume only one camera and one player
    }
}
