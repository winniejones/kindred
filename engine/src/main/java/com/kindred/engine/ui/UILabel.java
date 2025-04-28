package com.kindred.engine.ui;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

/**
 * A UI component for displaying text.
 */
@Slf4j
public class UILabel extends UIComponent {

    /**
     * -- SETTER --
     * Sets the text for the label.
     */
    @Setter
    public String text;
    /**
     * -- GETTER --
     * Gets the current font.
     */
    @Getter
    private Font font;
    // Size is determined by text and font, not usually set directly

    /**
     * Creates a label with default font and color.
     * @param position Position relative to the parent container.
     * @param text The text to display.
     */
    public UILabel(Vector2i position, String text) {
        super(position);
        this.font = new Font("Helvetica", Font.PLAIN, 12); // Default font
        this.text = text;
        this.backgroundColor = Color.WHITE; // Default color
    }

    /** Sets the font for the label. */
    public UILabel setFont(Font font) {
        this.font = font;
        return this; // Allow chaining
    }

    @Override
    public void render(Graphics g) {
        if (!active || text == null || text.isEmpty()) return; // Don't render if inactive or no text

        Vector2i absolutePosition = getAbsolutePosition();
        log.trace("Rendering Label '{}': RelativePos={}, Offset={}, Calculated AbsolutePos={}",
                text, position, offset, absolutePosition);
        g.setColor(this.backgroundColor);
        g.setFont(this.font);
        // Draw string at baseline; position usually refers to top-left for components
        // Adjust y position by font ascent to draw relative to top-left
        int yPos = absolutePosition.y + g.getFontMetrics().getAscent();
        g.drawString(text, absolutePosition.x, yPos);
    }

    // Update method is likely not needed for a static label
    @Override
    public void update() {
        // No default update behavior
    }
}
