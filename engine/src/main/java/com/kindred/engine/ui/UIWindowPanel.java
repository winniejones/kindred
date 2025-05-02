package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

import static com.kindred.engine.ui.UIWindowPanel.Placement.CENTER;
import static com.kindred.engine.ui.UIWindowPanel.Placement.START;

/**
 * A specialized UIPanel that renders with 3D-style borders and an optional header bar,
 * similar to classic RPG UIs. Child components are positioned within the content area.
 */
@Slf4j
public class UIWindowPanel extends UIPanel {

    // Style properties
    private Color borderLightColor;
    private Color borderDarkColor;
    private Color headerColor; // Optional header background
    private String title = "";
    private Font titleFont;
    private Color titleColor;
    private Placement titlePlacement = START;
    private int headerHeight = 0; // 0 means no header
    private int borderSize = 2;   // Thickness of the 3D border

    public enum Placement {
        START, CENTER, END;
    }
    private UIButton closeButton;

    /**
     * Creates a new UIWindowPanel.
     * @param position Position relative to its parent (or screen).
     * @param size Outer dimensions of the panel (including borders/header).
     */
    public UIWindowPanel(Vector2i position, Vector2i size) {
        super(position, size); // Call UIPanel constructor

        // Default styling (can be overridden with setters)
        setColor(Const.COLOR_STONE_700); // Use a base background color (e.g., dark stone)
        setBorderColors(Const.COLOR_STONE_500, Const.COLOR_STONE_900); // Lighter/Darker shades
        setHeaderColor(Const.COLOR_BLUE_700); // Example: Blue header
        setTitleColor(Color.WHITE);
        setTitleFont(Const.FONT_SANS_BOLD_12);
        setHeaderHeight(20); // Example header height
        setTitle(""); // No title by default

        addInternalCloseButton();
    }

    /** Helper method to create and add the close button */
    private void addInternalCloseButton() {
        // Only add if header exists and size is reasonable
        if (headerHeight > 0 && size.x > Const.CLOSE_BTN_SIZE + borderSize * 2 + 6 && size.y > Const.CLOSE_BTN_SIZE + borderSize * 2) {
            Vector2i btnSize = new Vector2i(Const.CLOSE_BTN_SIZE, Const.CLOSE_BTN_SIZE);
            // Position relative to the panel's top-right corner, inside border, centered in header vertically
            int btnX = this.size.x - btnSize.x - borderSize - 3; // 3px padding from right border
            int btnY = borderSize + (headerHeight - Const.CLOSE_BTN_SIZE * 2) / 2; // Center vertically in header area
            Vector2i btnPos = new Vector2i(btnX, btnY);

            // Create the button, action sets this panel's active state to false
            this.closeButton = new UIButton(btnPos, btnSize, "X", () -> this.setActive(false))
                    .setColor(Const.COLOR_STONE_900)
                    .setFont(Const.FONT_SANS_BOLD_7);

            // Add the button as a child component of this panel
            this.addComponent(this.closeButton);
        } else {
             log.warn("Panel too small or no header, cannot add close button.");
        }
    }

    // --- Fluent Setters for Styling ---

    public UIWindowPanel setBorderSize(int size) {
        this.borderSize = Math.max(1, size);
        return this;
    }

    public UIWindowPanel setBorderColors(Color light, Color dark) {
        this.borderLightColor = light;
        this.borderDarkColor = dark;
        return this;
    }

    public UIWindowPanel setHeaderHeight(int height) {
        this.headerHeight = Math.max(0, height);
        return this;
    }

    public UIWindowPanel setHeaderColor(Color color) {
        this.headerColor = color;
        return this;
    }

    public UIWindowPanel setTitle(String title) {
        this.title = (title != null) ? title : "";
        return this;
    }

    public UIWindowPanel setTitlePlacement(Placement placement) {
        this.titlePlacement = (placement != null) ? placement : START;
        return this;
    }

    public UIWindowPanel setTitleFont(Font font) {
        this.titleFont = font;
        return this;
    }

    public UIWindowPanel setTitleColor(Color color) {
        this.titleColor = color;
        return this;
    }

    // Override background color setter to maybe adjust borders automatically? (Optional)
    @Override
    public UIWindowPanel setColor(Color color) {
        super.setColor(color);
        // Optionally derive border colors if not set explicitly
        if (this.borderLightColor == null || this.borderDarkColor == null) {
            deriveBorderColors(color);
        }
        // Optionally set header color based on background if not set
        if (this.headerColor == null) {
            this.headerColor = color.darker(); // Example derivation
        }
        return this;
    }

    @Override public UIWindowPanel setBackgroundColor(Color color) { super.setBackgroundColor(color); return this; }
    @Override public UIWindowPanel setPosition(Vector2i position) { super.setPosition(position); return this; }
    @Override public UIWindowPanel setPosition(int x, int y) { super.setPosition(x, y); return this; }
    @Override public UIWindowPanel setSize(Vector2i size) { super.setSize(size); return this; }
    @Override public UIWindowPanel setSize(int width, int height) { super.setSize(width, height); return this; }
    @Override public UIWindowPanel setActive(boolean active) { super.setActive(active); return this; }

    private void deriveBorderColors(Color base) {
        if (base == null) {
            this.borderLightColor = Color.LIGHT_GRAY;
            this.borderDarkColor = Color.DARK_GRAY;
        } else {
            this.borderLightColor = base.brighter();
            this.borderDarkColor = base.darker();
        }
    }

    // --- Logic ---

    /** Calculates the top-left corner of the content area (inside borders/header). */
    public Vector2i getContentAreaPosition() {
        Vector2i absPos = getAbsolutePosition();
        absPos.x += borderSize;
        absPos.y += borderSize + headerHeight;
        return absPos;
    }

    /** Calculates the size of the content area. */
    public Vector2i getContentAreaSize() {
        int contentWidth = Math.max(0, size.x - borderSize * 2);
        int contentHeight = Math.max(0, size.y - borderSize * 2 - headerHeight);
        return new Vector2i(contentWidth, contentHeight);
    }


    /**
     * Updates the panel. Overrides UIPanel to pass the correct
     * content area offset to child components.
     */
    @Override
    public void update(InputState input, float deltaTime) {
        if (!active) return;
        Vector2i contentAreaPos = getContentAreaPosition(); // Offset for most children
        Vector2i headerAreaPos = getAbsolutePosition(); // Offset for header buttons
        headerAreaPos.x += borderSize;
        headerAreaPos.y += borderSize;

        // Update children
        for (int i = components.size() - 1; i >= 0; i--) {
            UIComponent component = components.get(i);
            if (component.active) {
                // Buttons directly in header use header offset, others use content offset
                if (component == closeButton) { // Check if it's the close button
                     component.setOffset(getAbsolutePosition()); // Close button relative to panel corner
                } else {
                     component.setOffset(contentAreaPos); // Other children relative to content area
                }
                component.update(input, deltaTime);
            }
        }
    }


    /**
     * Renders the panel with 3D borders, header, title, and children.
     * @param g The Graphics context to draw on.
     */
    @Override
    public void render(Graphics g) {
        if (!active) return;

        Vector2i absolutePos = getAbsolutePosition();
        int x = absolutePos.x;
        int y = absolutePos.y;
        int w = size.x;
        int h = size.y;

        // Ensure colors are set
        if (borderLightColor == null || borderDarkColor == null) deriveBorderColors(this.color);
        if (headerColor == null) headerColor = (this.color != null) ? this.color.brighter() : Color.GRAY;
        if (titleColor == null) titleColor = Color.WHITE;
        if (titleFont == null) titleFont = Const.FONT_SANS_BOLD_12; // Use a default

        // --- Draw Background ---
        if (this.color != null) {
            g.setColor(this.color);
            g.fillRect(x + borderSize, y + borderSize + headerHeight, w - borderSize * 2, h - borderSize * 2 - headerHeight);
        }

        // --- Draw Header ---
        if (headerHeight > 0) {
            g.setColor(headerColor);
            g.fillRect(x + borderSize, y + borderSize, w - borderSize * 2, headerHeight);
            // Draw Title Text (centered in header)
            if (!title.isEmpty()) {
                Font oldFont = g.getFont();
                Color oldColor = g.getColor();
                g.setFont(titleFont);
                g.setColor(titleColor);
                FontMetrics fm = g.getFontMetrics();
                int titleWidth = fm.stringWidth(title);
                int titleX = getTitleX(x, w, titleWidth);
                int titleY = y + borderSize + (headerHeight - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(title, titleX, titleY);
                g.setFont(oldFont);
                g.setColor(oldColor);
            }
        }

        // --- Draw 3D Borders ---
        g.setColor(borderLightColor);
        // Top border lines
        for(int i = 0; i < borderSize; i++) {
            g.drawLine(x + i, y + i, x + w - 1 - i, y + i); // Top
            g.drawLine(x + i, y + i + 1, x + i, y + h - 1 - i); // Left (start one pixel down)
        }

        g.setColor(borderDarkColor);
        // Bottom border lines
        for(int i = 0; i < borderSize; i++) {
            g.drawLine(x + i + 1, y + h - 1 - i, x + w - 1 - i, y + h - 1 - i); // Bottom
            g.drawLine(x + w - 1 - i, y + i + 1, x + w - 1 - i, y + h - 2 - i); // Right (adjust start/end Y)
        }


        // --- Render Child Components ---
        // Children render themselves relative to the content area origin passed via setOffset
        // Need to set clip for children so they don't draw outside the content area
        Rectangle originalClip = g.getClipBounds();
        // Clip includes header and content area, but stays within outer border
        g.setClip(x + borderSize, y + borderSize, w - borderSize * 2, h - borderSize * 2);

        for (UIComponent component : components) {
            if (component.active) {
                component.render(g);
            }
        }
        // Restore original clip
        g.setClip(originalClip);
    }

    private int getTitleX(int x, int w, int titleWidth) {
        switch (titlePlacement) {
            case START -> {
                return x + Const.MARGIN_2 + 2;
            }
            case END -> {
                return x + borderSize + (w - borderSize * 2 - titleWidth) - Const.MARGIN_2;
            }
            default -> {
                return x + borderSize + (w - borderSize * 2 - titleWidth) / 2;
            }
        }
    }
}
