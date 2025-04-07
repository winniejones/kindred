package com.kindred.engine.level;

public class SpawnPoint {
    /** Enum defining the types of entities that can be spawned from map markers. */
    public enum SpawnType {
        PLAYER,
        NPC_VILLAGER,
        ENEMY_SLIME
        // Add other types as needed
    }

    private final int tileX;      // Tile X coordinate of the spawn point
    private final int tileY;      // Tile Y coordinate of the spawn point
    private final SpawnType type; // Type of entity to spawn

    public SpawnPoint(int tileX, int tileY, SpawnType type) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.type = type;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public SpawnType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "SpawnPoint{" +
                "tileX=" + tileX +
                ", tileY=" + tileY +
                ", type=" + type +
                '}';
    }
}
