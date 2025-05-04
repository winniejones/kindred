package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A UI component that displays multiple lines of text, suitable for a chat log.
 * Handles basic line wrapping and displays a fixed number of recent lines.
 */
@Slf4j
public class UIChatDisplay extends UIComponent {

    private List<String> lines = new LinkedList<>(); // Use LinkedList for efficient addition/removal from start
    public Vector2i size; // Explicit size required
    private Font font;
    private int maxLines = 10; // Max lines to store/display
    private int lineHeight = 0; // Calculated from font
    private int lineSpacing = 2; // Extra pixels between lines

    public UIChatDisplay(Vector2i position, Vector2i size) {
        super(position);
        if (size == null || size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("UITextArea size must be positive.");
        }
        this.size = size;
        this.font = Const.FONT_SANS_PLAIN_9; // Default font
        this.backgroundColor = Const.COLOR_STONE_100; // Default text color
        // Background color is handled by the parent UIPanel
    }

    public UIChatDisplay setFont(Font font) {
        this.font = font;
        this.lineHeight = 0; // Force recalculation
        return this;
    }

    public UIChatDisplay setMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
        return this;
    }

    public UIChatDisplay setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
        return this;
    }
    public UIChatDisplay setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    @Override public UIChatDisplay setBackgroundColor(Color color) { super.setBackgroundColor(color); return this; }
    @Override public UIChatDisplay setBackgroundColor(int color) { super.setBackgroundColor(color); return this; }
    @Override public UIChatDisplay setColor(Color color) { super.setColor(color); return this; }
    @Override public UIChatDisplay setColor(int color) { super.setColor(color); return this; }
    @Override public UIChatDisplay setActive(boolean active) { super.setActive(active); return this; }
    @Override public UIChatDisplay setPosition(Vector2i position) { super.setPosition(position); return this; }
    @Override public UIChatDisplay setPosition(int x, int y) { super.setPosition(x, y); return this; }

    /**
     * Adds a new message to the text area. Handles basic word wrapping
     * and removes old lines if exceeding maxLines.
     * @param message The message string to add.
     */
    public synchronized UIChatDisplay addLine(String message) {
        if (message == null || message.isEmpty()) {
            return this;
        }

        // Basic Word Wrapping (Needs Graphics context for accurate width)
        // For simplicity now, we'll just add the line and assume it fits or rely on clipping.
        // TODO: Implement proper word wrapping based on FontMetrics and size.x
        lines.add(message);

        // Trim old lines if exceeding max capacity
        while (lines.size() > maxLines) {
            ((LinkedList<String>) lines).removeFirst(); // Remove oldest line
        }
        log.trace("Added line: '{}'. Total lines: {}", message, lines.size());
        return this;
    }

    @Override
    public void render(Graphics g) {
        if (!active) return;

        Vector2i absolutePos = getAbsolutePosition();
        int x = absolutePos.x;
        int y = absolutePos.y;

        // Store original settings
        Color originalColor = g.getColor();
        Font originalFont = g.getFont();
        Rectangle originalClip = g.getClipBounds(); // Get current clip

        try {
            // Set clip bounds to prevent drawing outside the text area
            g.setClip(x, y, size.x, size.y);

            g.setFont(this.font);
            g.setColor(this.backgroundColor);
            FontMetrics fm = g.getFontMetrics();

            // Calculate line height if not already done
            if (lineHeight <= 0) {
                lineHeight = fm.getHeight();
            }

            // Calculate where to start drawing (bottom-up)
            int drawY = y + size.y - fm.getDescent() - lineSpacing; // Start near bottom

            // Draw lines in reverse order (newest at bottom)
            synchronized (this) { // Synchronize access to lines list
                List<String> linesToDraw = new ArrayList<>(lines); // Copy to avoid issues if modified during render
                for (int i = linesToDraw.size() - 1; i >= 0; i--) {
                    if (drawY < y + fm.getAscent()) break; // Stop if we go above the top edge

                    String line = linesToDraw.get(i);
                    g.drawString(line, x + 2, drawY); // Add small X padding
                    drawY -= (lineHeight + lineSpacing); // Move up for next line
                }
            }

        } finally {
            // Restore original settings
            g.setColor(originalColor);
            g.setFont(originalFont);
            g.setClip(originalClip); // Restore original clip area
        }
    }

    @Override
    public void update(InputState input, float deltaTime) {
        // Usually static text area doesn't need update logic
    }
}
