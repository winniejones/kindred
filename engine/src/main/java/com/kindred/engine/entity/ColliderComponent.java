package com.kindred.engine.entity;

/**
 * Component storing collision hitbox dimensions for an entity.
 * Assumes the hitbox is aligned with the entity's top-left position (pos.x, pos.y).
 */
public class ColliderComponent implements Component {
    public int hitboxWidth;
    public int hitboxHeight;
    // Optional: Add offsets if the hitbox isn't aligned with pos.x, pos.y
    public int offsetX = 0;
    public int offsetY = 0;

    /**
     * Constructor with explicit offset.
     * @param hitboxWidth Width of the hitbox.
     * @param hitboxHeight Height of the hitbox.
     * @param offsetX Horizontal offset from the entity's x position.
     * @param offsetY Vertical offset from the entity's y position.
     */
    public ColliderComponent(int hitboxWidth, int hitboxHeight, int offsetX, int offsetY) {
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Constructor assuming zero offset (hitbox top-left aligns with entity position).
     * @param hitboxWidth Width of the hitbox.
     * @param hitboxHeight Height of the hitbox.
     */
    public ColliderComponent(int hitboxWidth, int hitboxHeight) {
        this(hitboxWidth, hitboxHeight, 0, 0); // Calls the other constructor with 0 offset
    }

    // Getters could be added if preferred over direct access
    public int getHitboxX(PositionComponent pos) {
        return pos.x + offsetX;
    }

    public int getHitboxY(PositionComponent pos) {
        return pos.y + offsetY;
    }
}
