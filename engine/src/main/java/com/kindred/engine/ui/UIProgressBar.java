package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

/**
 * A UI component that displays a progress bar (e.g., for health, XP, mana).
 */
@Slf4j
public class UIProgressBar extends UIComponent {

    /**
     * -- GETTER --
     * Gets the current progress value (0.0 to 1.0).
     */
    @Getter
    private double progress = 0.0; // Value between 0.0 and 1.0
    public Vector2i size;          // Explicit size required for the bar
    private Color foregroundColor; // Color of the filled portion
    /**
     * -- SETTER --
     * Sets whether the background track should be drawn.
     */
    @Setter
    private boolean drawBackground = true; // Whether to draw the background track

    /**
     * Creates a new progress bar.
     * @param position Position relative to parent.
     * @param size Dimensions of the progress bar.
     */
    public UIProgressBar(Vector2i position, Vector2i size) {
        super(position);
        if (size == null || size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("Progress bar size must be positive.");
        }
        this.size = size;
        // Default colors
        this.backgroundColor = Color.DARK_GRAY; // Background track
        this.foregroundColor = Color.GREEN;     // Filled portion (like health)
        this.progress = 0.0; // Start empty or full? Let's start empty.
    }

    /**
     * Sets the progress value (0.0 to 1.0).
     * Clamps the value to the valid range.
     * @param progress The progress value (0.0 to 1.0).
     */
    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
    }

    /** Sets the color for the filled portion of the bar. */
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }
    /** Sets the color for the filled portion of the bar using ARGB int. */
    public void setForegroundColor(int color) {
        this.foregroundColor = new Color(color, true);
    }

    // Update method likely not needed unless the bar animates itself
    @Override
    public void update(InputState input, float deltaTime) {
        // Base implementation does nothing
    }

    @Override
    public void render(Graphics g) {
        if (!active) return;

        Vector2i absolutePos = getAbsolutePosition();
        int x = absolutePos.x;
        int y = absolutePos.y;

        // <<< Add Logging >>>
        log.trace("Rendering ProgressBar: RelativePos={}, Offset={}, Calculated AbsolutePos=({}, {}), Size=({}, {})",
                position, offset, x, y, size.x, size.y);
        // <<< End Logging >>>

        // 1. Draw Background Track (Optional)
        if (drawBackground && backgroundColor != null) {
            g.setColor(backgroundColor);
            g.fillRect(x, y, size.x, size.y);
        }

        // 2. Draw Foreground (Filled Portion)
        int filledWidth = (int) (progress * size.x);
        if (filledWidth > 0 && foregroundColor != null) {
            g.setColor(foregroundColor);
            g.fillRect(x, y, filledWidth, size.y);
        }

        // 3. Optional: Draw Border
        // g.setColor(Color.BLACK);
        // g.drawRect(x, y, size.x, size.y);
    }
}
