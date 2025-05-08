package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

import static com.kindred.engine.ui.UIRenderHelper.drawBorder;
import static com.kindred.engine.ui.UIWindowPanel.Placement.START;

/**
 * A specialized UIPanel that renders with 3D-style borders and an optional header bar,
 * similar to classic RPG UIs. Child components are positioned within the content area.
 */
@Slf4j
public class UIWindowPanel extends UIPanel {

    private Color borderLightColor;
    private Color borderDarkColor;
    private Color headerColor;
    private String title = "";
    private Font titleFont;
    private Color titleColor;
    private Placement titlePlacement = START;
    @Getter
    private int headerHeight = 10;
    @Getter
    private int outerBorderSize = 2;
    @Getter
    private int innerBorderSize = 1;
    private UIButton closeButton;

    public enum Placement {
        START, CENTER, END;
    }

    /**
     * Creates a new UIWindowPanel.
     * @param position Position relative to its parent (or screen).
     * @param size Outer dimensions of the panel (including borders/header).
     */
    public UIWindowPanel(Vector2i position, Vector2i size, Runnable onMainButtonUntoggleAction) {
        super(position, size); // Call UIPanel constructor

        // Default styling (can be overridden with setters)
        setColor(Const.COLOR_STONE_700); // Use a base background color (e.g., dark stone)
        setBorderColors(Const.COLOR_STONE_500, Const.COLOR_STONE_900); // Lighter/Darker shades
        setHeaderColor(Const.COLOR_STONE_700); // Example: Blue header
        setTitleColor(Color.WHITE);
        setTitleFont(Const.FONT_SANS_BOLD_12);
        setHeaderHeight(20); // Example header height
        setTitle(""); // No title by default

        addInternalCloseButton(onMainButtonUntoggleAction);
    }

    /** Helper method to create and add the close button */
    private void addInternalCloseButton(Runnable onMainButtonUntoggleAction) {
        if (headerHeight > 0 && size.x > Const.CLOSE_BTN_SIZE + outerBorderSize * 2 + 6 && size.y > Const.CLOSE_BTN_SIZE + outerBorderSize * 2) {
            Vector2i btnSize = new Vector2i(Const.CLOSE_BTN_SIZE, Const.CLOSE_BTN_SIZE);
            // Position relative to the panel's top-right corner, inside border, centered in header vertically
            int btnX = this.size.x - btnSize.x - outerBorderSize - 3; // 3px padding from right border
            int btnY = 3; // Center vertically in header area
            Vector2i btnPos = new Vector2i(btnX, btnY);

            // Create the button, action sets this panel's active state to false
            this.closeButton = new UIButton(btnPos, btnSize, "X", () -> {
                this.setActive(false);
                if (onMainButtonUntoggleAction != null) {
                    onMainButtonUntoggleAction.run(); // Untoggle the corresponding menu button
                }
            })
                    .setBaseColor(Const.COLOR_STONE_900)
                    .setBackgroundColor(Const.COLOR_STONE_900)
                    .setHoverColor(Const.COLOR_STONE_950)
                    .setColor(Const.COLOR_STONE_100)
                    .setFont(Const.FONT_SANS_BOLD_7);

            // Add the button as a child component of this panel
            this.addComponent(this.closeButton);
        } else {
            log.warn("Panel too small or no header, cannot add close button.");
        }
    }

    // --- Fluent Setters ---
    public UIWindowPanel setOuterBorderSize(int size) {
        this.outerBorderSize = Math.max(1, size);
        return this;
    }

    public UIWindowPanel setInnerBorderSize(int size) {
        this.innerBorderSize = Math.max(0, size);
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
            this.headerColor = backgroundColor.darker(); // Example derivation
        }
        return this;
    }

    @Override
    public UIWindowPanel setBackgroundColor(Color color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public UIWindowPanel setPosition(Vector2i position) {
        super.setPosition(position);
        return this;
    }

    @Override
    public UIWindowPanel setPosition(int x, int y) {
        super.setPosition(x, y);
        return this;
    }

    @Override
    public UIWindowPanel setSize(Vector2i size) {
        super.setSize(size);
        return this;
    }

    @Override
    public UIWindowPanel setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public UIWindowPanel setActive(boolean active) {
        super.setActive(active);
        return this;
    }

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
        absPos.x += outerBorderSize + innerBorderSize;
        absPos.y += outerBorderSize + headerHeight + innerBorderSize;
        return absPos;
    }

    /** Calculates the size of the content area. */
    public Vector2i getContentAreaSize() {
        int borderTotal = outerBorderSize + innerBorderSize;
        int contentWidth = Math.max(0, size.x - borderTotal * 2);
        int contentHeight = Math.max(0, size.y - borderTotal * 2 - headerHeight);
        return new Vector2i(contentWidth, contentHeight);
    }


    /**
     * Updates the panel. Overrides UIPanel to pass the correct
     * content area offset to child components.
     */
    @Override
    public void update(InputState input, float deltaTime) {
        if (!active) return;
        Vector2i contentAreaOriginOffset = getAbsolutePosition(); // Get panel's screen origin
        contentAreaOriginOffset.x += outerBorderSize + innerBorderSize; // Adjust for borders/header/inner border
        contentAreaOriginOffset.y += outerBorderSize + headerHeight + innerBorderSize;

        Vector2i headerButtonOffset = getAbsolutePosition(); // Offset for close button is just panel origin

        // Update children
        for (int i = components.size() - 1; i >= 0; i--) {
            UIComponent component = components.get(i);
            if (component.active) {
                // Buttons directly in header use header offset, others use content offset
                if (component == closeButton) { // Check if it's the close button
                    component.setOffset(headerButtonOffset); // Close button relative to panel corner
                } else {
                    component.setOffset(contentAreaOriginOffset); // Other children relative to content area
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
        if (headerColor == null)
            headerColor = (this.backgroundColor != null) ? this.backgroundColor.darker() : Color.GRAY;
        if (titleColor == null) titleColor = Color.WHITE;
        if (titleFont == null) titleFont = Const.FONT_SANS_BOLD_12;

        // Define content area bounds (inside outer border and header)
        int contentX = x + outerBorderSize;
        int contentY = y + outerBorderSize + headerHeight;
        int contentW = w - outerBorderSize * 2;
        int contentH = h - outerBorderSize * 2 - headerHeight;

        // --- Draw Background for Content Area ---
        if (this.color != null && contentW > 0 && contentH > 0) {
            g.setColor(this.color);
            g.fillRect(contentX, contentY, contentW, contentH);
        }

        // --- Draw Header ---
        if (headerHeight > 0 && contentW > 0) {
            renderHeader(g, contentX, y, contentW, x, w);
        }

        // --- Draw Outer Borders ---
        drawBorder(g, x, y, w, h, outerBorderSize, borderLightColor, borderDarkColor);

        // --- Draw Inner Content Frame ---
        if (innerBorderSize > 0 && contentW > 0 && contentH > 0) {
            drawBorder(g, contentX, contentY, contentW, contentH, innerBorderSize, borderDarkColor, borderLightColor);
        }

        // --- Render Child Components ---
        renderChildComponents(g, contentX, contentY, contentW, contentH);
    }

    private void renderHeader(Graphics g, int contentX, int y, int contentW, int x, int w) {
        g.setColor(headerColor);
        g.fillRect(contentX, y + outerBorderSize, contentW, headerHeight);
        // Draw Title Text
        if (!title.isEmpty()) {
            Font oldFont = g.getFont();
            Color oldColor = g.getColor();
            g.setFont(titleFont);
            g.setColor(titleColor);
            FontMetrics fm = g.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            int titleX = getTitleX(x, w, titleWidth);
            int titleY = y + outerBorderSize + (headerHeight - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(title, titleX, titleY);
            g.setFont(oldFont);
            g.setColor(oldColor);
        }
    }

    private void renderChildComponents(Graphics g, int contentX, int contentY, int contentW, int contentH) {
        Rectangle originalClip = g.getClipBounds();
        try {
            // Clip children to the content area (inside the inner border)
            //g.setClip(contentX + innerBorderSize, contentY + innerBorderSize,
            //        contentW - innerBorderSize * 2, contentH - innerBorderSize * 2);
            Rectangle contentClip = new Rectangle(contentX + innerBorderSize, contentY + innerBorderSize,
                    Math.max(0, contentW - innerBorderSize * 2), Math.max(0, contentH - innerBorderSize * 2));


            for (UIComponent component : components) {
                if (component.active) {
                    // Special case: Render close button even if slightly outside content clip (it's in header)
                    if (component == closeButton) {
                        Graphics gButton = g.create(); // Create copy for button
                        gButton.setClip(originalClip); // Restore original clip for button rendering
                        try {
                            component.render(gButton);
                        } finally {
                            gButton.dispose();
                        }
                    } else {
                        // Render other components, clipped to the content area
                        Graphics gContent = g.create();
                        try {
                            gContent.clipRect(contentClip.x, contentClip.y, contentClip.width, contentClip.height);
                            component.render(gContent);
                        } finally {
                            gContent.dispose();
                        }
                    }
                }
            }
        } finally {
            // Restore original clip
            g.setClip(originalClip);
        }
    }

    private int getTitleX(int x, int w, int titleWidth) {
        switch (titlePlacement) {
            case START -> {
                return x + Const.MARGIN_2 + 2;
            }
            case END -> {
                return x + outerBorderSize + (w - outerBorderSize * 2 - titleWidth) - Const.MARGIN_2;
            }
            default -> {
                return x + outerBorderSize + (w - outerBorderSize * 2 - titleWidth) / 2;
            }
        }
    }
}
