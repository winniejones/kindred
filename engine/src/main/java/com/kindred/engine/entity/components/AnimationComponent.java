package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;
import com.kindred.engine.resource.AssetLoader;

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

    /**
     * Constructor using framesPerSecond (preferred).
     * @param frames Animation frames [direction][frameIndex].
     * @param framesPerSecond How many animation frames to display per second.
     */
    public AnimationComponent(BufferedImage[][] frames, float framesPerSecond) {
        this.frames = frames;
        this.framesPerSecond = Math.max(0.1f, framesPerSecond); // Ensure positive FPS
    }

    /**
     * Constructor using frameDelay (ticks per frame).
     * @param frames Animation frames [direction][frameIndex].
     * @param frameDelay Number of update ticks before advancing frame.
     */
    @Deprecated
    public AnimationComponent(BufferedImage[][] frames, int frameDelay) {
        this.frames = frames;
        // Estimate FPS based on delay (assuming 60 UPS)
        if (frameDelay > 0) {
            this.framesPerSecond = 60.0f / frameDelay;
        } else {
            this.framesPerSecond = 1; // Avoid division by zero
        }
    }

    /** Gets the current frame based on direction and frame index. Handles nulls/bounds. */
    public BufferedImage getCurrentFrame() {
        // Basic bounds checking
        if (frames == null || direction < 0 || direction >= frames.length || frames[direction] == null || frames[direction].length == 0) {
            return AssetLoader.createPlaceholderImage(32, 32); // Use AssetLoader's placeholder
        }
        // Ensure frame index is within bounds
        int currentFrameIndex = frame % frames[direction].length;
        BufferedImage currentFrame = frames[direction][currentFrameIndex];
        // Return placeholder if the specific frame is null
        return (currentFrame != null && currentFrame.getWidth() > 1) ? currentFrame : AssetLoader.createPlaceholderImage(32, 32);
    }

     /** Updates the animation frame based on frameDelay ticks. */
     public void update() { // Reverted to no-arg update
         // Check if there are frames to animate
         if (frames == null || direction < 0 || direction >= frames.length || frames[direction] == null || frames[direction].length <= 1) {
              frame = 0;
              tick = 0;
              return; // No animation needed
         }

         tick++;
         if (tick >= frameDelay) {
             tick = 0;
             frame = (frame + 1) % frames[direction].length; // Loop animation
         }
     }


    /** Sets the current animation direction. Resets frame index if direction changes. */
    public void setDirection(int newDirection) {
        // Basic bounds check for direction index (0 to 3 for 4 directions)
        if (newDirection < 0 || newDirection > 3) {
             newDirection = DOWN; // Default to Down
        }

        if (newDirection != direction) {
            this.direction = newDirection;
            this.frame = 0; // Reset frame index
            this.tick = 0;  // Reset frame timer/tick
        }
    }
}
