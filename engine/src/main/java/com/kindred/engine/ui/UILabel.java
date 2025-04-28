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


    public String text;

    @Getter
    private Font font;
    private int lineHeight = 0; // Cached line height

    /**
     * Creates a label with default font and color.
     * @param position Position relative to the parent container.
     * @param text The text to display.
     */
    public UILabel(Vector2i position, String text) {
        super(position);
        this.font = new Font("Helvetica", Font.PLAIN, 12); // Default font
        this.text = text;
        this.color = Color.WHITE; // Default color
    }

    /** Sets the font for the label. */
    public UILabel setFont(Font font) {
        this.font = font;
        return this; // Allow chaining
    }
    public UILabel setText(String text) {
        this.text = text;
        return this;
    }
    @Override
    public UILabel setBackgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }
    @Override
    public UILabel setBackgroundColor(int color) {
        this.backgroundColor = new Color(color, true);
        return this;
    }

    // Override setters from UIComponent to return UILabel for chaining
    @Override public UILabel setColor(Color color) { super.setColor(color); return this; }
    @Override public UILabel setColor(int color) { super.setColor(color); return this; }
    @Override public UILabel setActive(boolean active) { super.setActive(active); return this; }
    @Override public UILabel setPosition(Vector2i position) { super.setPosition(position); return this; }
    @Override public UILabel setPosition(int x, int y) { super.setPosition(x, y); return this; }

    @Override
    public void render(Graphics g) {
        if (!active || text == null || text.isEmpty()) return; // Don't render if inactive or no text

        Vector2i absolutePosition = getAbsolutePosition();
        log.trace("Rendering Label '{}': RelativePos={}, Offset={}, Calculated AbsolutePos={}",
                text, position, offset, absolutePosition);
        g.setColor(this.color);
        g.setFont(this.font);
        // Draw string at baseline; position usually refers to top-left for components
        // Adjust y position by font ascent to draw relative to top-left
        int yPos = absolutePosition.y + g.getFontMetrics().getAscent();
        g.drawString(text, absolutePosition.x, yPos);
    }
}
