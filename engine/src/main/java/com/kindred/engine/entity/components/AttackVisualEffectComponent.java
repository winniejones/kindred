package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

import java.util.List;
import java.util.Map;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Component to manage the visual animation of an attack effect (e.g., a sword slash).
 * This is separate from the character's base animation.
 * It's added temporarily to an entity when it performs an attack that has a distinct visual effect.
 */
public class AttackVisualEffectComponent implements Component {

    /** The actual animation frames for the attack effect (e.g., slash sprites for the current direction). */
    public final BufferedImage[] frames;

    /** Hitbox data for each frame of this visual effect. Key: frameIndex, Value: List of relative Rectangles. */
    public final Map<Integer, List<Rectangle>> frameHitboxes;

    /** Duration each frame of this effect is shown (seconds). */
    public final float frameDuration;

    /** Total number of frames in this effect's animation sequence. */
    public final int totalFrames;

    /** Current frame index of the effect animation (0 to totalFrames - 1). */
    public int currentFrame = 0;

    /** Timer to track progression to the next frame of the effect (seconds). */
    public float animationTimer = 0f;

    /** The direction the attack visual effect should be oriented or chosen for.
     * This should match the AnimationComponent.direction of the attacker at the time of attack.
     */
    public final int direction;


    /**
     * Constructor for AttackVisualEffectComponent.
     *
     * @param effectFrames Animation frames for the visual effect (already selected for the correct direction).
     * @param hitboxes Hitbox data for each frame of the effect.
     * @param frameDuration Duration each frame is displayed.
     * @param totalFrames Total number of frames in the sequence.
     * @param attackDirection The direction the attack is facing, used to select correct hitboxes if they are direction-dependent.
     */
    public AttackVisualEffectComponent(BufferedImage[] effectFrames,
                                       Map<Integer, List<Rectangle>> hitboxes,
                                       float frameDuration,
                                       int totalFrames,
                                       int attackDirection) {
        this.frames = effectFrames;
        this.frameHitboxes = hitboxes; // These hitboxes are expected to be for the given attackDirection
        this.frameDuration = Math.max(0.01f, frameDuration); // Ensure positive duration
        this.totalFrames = Math.max(1, totalFrames); // Ensure at least one frame
        this.direction = attackDirection;
    }

    /**
     * Gets the current visual frame of the attack effect.
     * @return The BufferedImage for the current frame, or null if out of bounds or no frames.
     */
    public BufferedImage getCurrentVisualFrame() {
        if (frames != null && currentFrame >= 0 && currentFrame < frames.length) {
            return frames[currentFrame];
        }
        return null; // Or a placeholder
    }

    /**
     * Gets the hitboxes for the current visual frame of the attack effect.
     * @return A list of Rectangle objects for the current frame's hitboxes, or null/empty list if none.
     */
    public List<Rectangle> getCurrentHitboxes() {
        if (frameHitboxes != null && currentFrame >= 0 && currentFrame < totalFrames) { // Use totalFrames for map key consistency
            return frameHitboxes.get(currentFrame);
        }
        return null; // Or Collections.emptyList()
    }
}
