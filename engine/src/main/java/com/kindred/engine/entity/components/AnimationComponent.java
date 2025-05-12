package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;
import com.kindred.engine.resource.AssetLoader;

import java.util.List;
import java.util.Map;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AnimationComponent implements Component {

    // <<< Define Direction Constants >>>
    public static final int DOWN = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    // ---------------------------------

    public BufferedImage[][] frames; // frames[direction][frameIndex]
    public int direction = DOWN; // Start facing down
    public int frame = 0;
    public int tick = 0;
    /** Number of update ticks before advancing frame. */
    public int frameDelay;
    /** How many animation frames to display per second. */
    public float framesPerSecond = 5f;

    // --- Attack Animation State ---
    public boolean isAttacking = false;
    public int currentAttackFrame = 0;       // Current frame index for the attack animation
    public float attackAnimationTimer = 0f;  // Timer for current attack frame duration

    // --- Dynamically Set Attack Animation Data (transient: not saved/loaded directly) ---
    public transient BufferedImage[] activeAttackFramesForCurrentDirection; // Frames for the current attack, current direction
    public transient Map<Integer, List<Rectangle>> activeAttackFrameHitboxes; // Hitboxes for the current attack animation frames
    public transient float currentAttackFrameDuration; // Duration of each frame in the current attack
    public transient int currentAttackTotalFrames;   // Total number of frames in the current attack sequence


    /**
     * Constructor for walk/idle animations.
     * @param walkIdleFrames Animation frames [direction][frameIndex] for walking/idling.
     * @param walkIdleFps How many animation frames to display per second for walking/idling.
     */
    public AnimationComponent(BufferedImage[][] walkIdleFrames, float walkIdleFps) {
        this.frames = walkIdleFrames;
        this.framesPerSecond = Math.max(0.1f, walkIdleFps);
        // If you were using frameDelay for walk/idle:
        // this.frameDelay = (walkIdleFps > 0) ? (int) (60.0f / walkIdleFps) : Integer.MAX_VALUE;
    }

    /**
     * Gets the current frame for walking/idling.
     * Attack frames are typically set directly by AnimationSystem.
     */
    public BufferedImage getCurrentFrame() {
        if (frames == null || direction < 0 || direction >= frames.length || frames[direction] == null || frames[direction].length == 0) {
            return AssetLoader.createPlaceholderImage(32, 32);
        }
        int currentFrameIndex = frame % frames[direction].length;
        BufferedImage currentFrameSprite = frames[direction][currentFrameIndex];
        return (currentFrameSprite != null && currentFrameSprite.getWidth() > 1) ? currentFrameSprite : AssetLoader.createPlaceholderImage(32, 32);
    }

    /**
     * Updates the walk/idle animation frame based on frameDelay or FPS.
     * Attack animation progression is handled in AnimationSystem using deltaTime.
     */
    public void update() { // This is for walk/idle
        if (isAttacking) return; // Attack animation is handled by AnimationSystem with deltaTime

        if (frames == null || direction < 0 || direction >= frames.length || frames[direction] == null || frames[direction].length <= 1) {
            frame = 0;
            tick = 0;
            return;
        }

        // Assuming frameDelay is calculated from framesPerSecond (e.g., in constructor or here)
        int calculatedFrameDelay = (framesPerSecond > 0) ? (int) (60.0f / framesPerSecond) : Integer.MAX_VALUE; // Assuming 60 UPS for tick based
        // If you have a direct frameDelay field, use that instead.

        tick++;
        if (tick >= calculatedFrameDelay) {
            tick = 0;
            frame = (frame + 1) % frames[direction].length;
        }
    }


    /** Sets the current animation direction. Resets frame index if direction changes. */
    public void setDirection(int newDirection) {
        if (newDirection < 0 || newDirection > 3) {
            newDirection = DOWN;
        }
        if (newDirection != direction) {
            this.direction = newDirection;
            if (!isAttacking) { // Only reset walk/idle frame if not in an attack
                this.frame = 0;
                this.tick = 0;
            }
            // If attacking, the activeAttackFramesForCurrentDirection might need to be updated
            // if the attack animation can change direction mid-swing (more complex).
            // For now, assume attack direction is fixed at the start of the attack.
        }
    }

    /**
     * Starts an attack animation sequence.
     * Called by input systems (PlayerInputSystem, AISystem).
     * @param attackAnimationSet The full 2D array [direction][frame] for the chosen weapon/attack.
     * @param hitboxes The hitbox data for this specific attack animation (already filtered for the current direction).
     * @param frameDuration The duration each frame of this attack should be displayed.
     * @param totalAnimationFrames The total number of frames in one sequence of this attack.
     */
    public void startAttackAnimation(BufferedImage[][] attackAnimationSet, Map<Integer, List<Rectangle>> hitboxes, float frameDuration, int totalAnimationFrames) {
        this.isAttacking = true;
        this.currentAttackFrame = 0;
        this.attackAnimationTimer = 0f;

        if (attackAnimationSet != null && this.direction >= 0 && this.direction < attackAnimationSet.length) {
            // Store only the frames for the current direction the entity is facing
            this.activeAttackFramesForCurrentDirection = attackAnimationSet[this.direction];
        } else {
            this.activeAttackFramesForCurrentDirection = null; // Or a placeholder array
            System.err.println("Warning: Attack animation set or current direction invalid in startAttackAnimation.");
        }

        this.activeAttackFrameHitboxes = hitboxes;
        this.currentAttackFrameDuration = frameDuration;
        this.currentAttackTotalFrames = totalAnimationFrames;

        // Ensure totalAnimationFrames matches the length of the actual frames for the current direction if available
        if (this.activeAttackFramesForCurrentDirection != null) {
            this.currentAttackTotalFrames = this.activeAttackFramesForCurrentDirection.length;
        } else if (totalAnimationFrames <= 0 && attackAnimationSet != null && attackAnimationSet.length > 0 && attackAnimationSet[0] != null) {
             // Fallback if totalAnimationFrames wasn't correctly derived from registry, but attackAnimationSet is somewhat valid
            this.currentAttackTotalFrames = attackAnimationSet[0].length; // Assume all directions have same frame count
        }


        if (this.activeAttackFramesForCurrentDirection == null || this.activeAttackFramesForCurrentDirection.length == 0) {
            System.err.println("Error: No active attack frames for current direction: " + this.direction + ". Attack might not display.");
            this.isAttacking = false; // Cannot perform attack without frames
        }
    }
}
