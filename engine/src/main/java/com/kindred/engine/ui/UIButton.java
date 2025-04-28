package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import com.kindred.engine.ui.listener.UIActionListener;
import com.kindred.engine.ui.listener.UIButtonListener;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * A clickable UI button component. Can display text or an image.
 * Triggers an action when clicked.
 */
@Slf4j
public class UIButton extends UIComponent {

    public Vector2i size;
    public UILabel label;
    private BufferedImage image = null;

    private final Color colorOnHover = new Color(1, 1, 1, 0.7f);
    private final Color baseColor;

    private final UIActionListener actionListener;
    @Setter
    private UIButtonListener buttonListener;

    // State tracking
    private boolean inside = false;
    private boolean pressed = false;
    private boolean ignorePressed = false;

    private int arcWidth = 3;
    private int arcHeight = 3;

    /**
     * Creates a button with text.
     */
    public UIButton(Vector2i position, Vector2i size, String text, UIActionListener actionListener) {
        this(position, size, text, Color.WHITE, actionListener);
    }

    public UIButton(Vector2i position, Vector2i size, String text, Color baseColor, UIActionListener actionListener) {
        super(position);
        if (size == null || size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("Button size must be positive.");
        }
        this.size = size;
        this.actionListener = actionListener;

        // Create the label but DON'T add it to the panel here.
        // Position will be relative to the button's top-left (0,0) for rendering calculation.
        this.label = new UILabel(new Vector2i(0, 0), text); // Initial position (0,0) relative
        this.label.setBackgroundColor(Color.WHITE); // Dark gray text
        this.label.active = true;
        // We will calculate the centered position within the render method
        this.baseColor = baseColor;
        this.backgroundColor = baseColor;
        initDefaultListener();
    }

    /**
     * Creates a button using an image.
     */
    public UIButton(Vector2i position, BufferedImage image, UIActionListener actionListener) {
        super(position);
        if (image == null) {
            throw new IllegalArgumentException("Button image cannot be null");
        }
        this.size = new Vector2i(image.getWidth(), image.getHeight());
        this.actionListener = actionListener;
        this.image = image;
        this.label = null;
        initDefaultListener();
        this.baseColor = this.backgroundColor;
    }

    private void initDefaultListener() {
        setBackgroundColor(0xaaaaaa);
        this.buttonListener = new UIButtonListener();
    }

    /** Sets the text for the button's label. */
    public void setText(String text) {
        if (label != null) {
            label.setText(text);
        } else if (image == null && text != null && !text.isEmpty()) {
            // If it was an image button, but now setting text, create a label
            label = new UILabel(new Vector2i(0, 0), text);
            label.setBackgroundColor(0x444444);
            label.active = true;
        }
    }

    /** Sets the font for the button's label. */
    public void setFont(Font font) {
        if (label != null) {
            label.setFont(font);
        }
    }

    public void setLabelColor(Color color) {
        if (label != null) {
            this.label.setBackgroundColor(color);
        }
    }

    /** Sets the corner arc size. */
    public void setArc(int arcWidth, int arcHeight) {
        this.arcWidth = Math.max(0, arcWidth);
        this.arcHeight = Math.max(0, arcHeight);
    }

    /** Update method - Handles mouse interaction logic using InputState. */
    @Override
    public void update(InputState input, float deltaTime) {
        if (!active) return;

        // Get mouse state from the passed-in InputState object
        Point mousePos = input.getMousePosition();
        boolean leftMouseButtonDown = input.isButtonDown(MouseEvent.BUTTON1);
        boolean leftMouseButtonPressed = input.isButtonPressed(MouseEvent.BUTTON1); // Pressed this frame
        boolean leftMouseButtonReleased = input.isButtonReleased(MouseEvent.BUTTON1); // Released this frame

        Vector2i absolutePos = getAbsolutePosition();
        Rectangle bounds = new Rectangle(absolutePos.x, absolutePos.y, size.x, size.y);

        boolean currentlyInside = bounds.contains(mousePos);

        // --- Handle State Changes ---
        if (currentlyInside) {
            if (!inside) { // Mouse Entered
                log.trace("Mouse entered button {}", this.label != null ? label.text : "ImageButton");
                this.setBackgroundColor(this.colorOnHover);
                buttonListener.entered(this);
                // If mouse button is already down when entering, ignore the subsequent release
                ignorePressed = leftMouseButtonDown;
            }
            inside = true;

            // Check for Press: Mouse button went down *this frame* while inside
            if (!pressed && leftMouseButtonPressed && !ignorePressed) {
                log.trace("Mouse pressed on button {}", this.label != null ? label.text : "ImageButton");
                buttonListener.pressed(this);
                pressed = true;
            }
            // Check for Release: Mouse button went up *this frame* while inside
            else if (pressed && leftMouseButtonReleased) {
                log.trace("Mouse released on button {}", this.label != null ? label.text : "ImageButton");
                buttonListener.released(this);
                actionListener.perform(); // Trigger the action!
                pressed = false; // Reset pressed state immediately after action
                // After release, mouse is still inside, so trigger entered state again
                buttonListener.entered(this);
            }
        } else { // Mouse is Outside
            if (inside) { // Mouse just exited
                log.trace("Mouse exited button {}", this.label != null ? label.text : "ImageButton");
                buttonListener.exited(this); // <<< Crucial: Reset color on exit
                this.setBackgroundColor(this.baseColor);
                // Reset pressed state if mouse was dragged out while pressed
                if (pressed) {
                    // No action triggered, just reset state
                    pressed = false;
                }
            }
            inside = false;
            // Ensure pressed is false if mouse is outside
            if (pressed) pressed = false;
        }

        // If mouse button is released anywhere, reset ignorePressed flag
        if (leftMouseButtonReleased) {
            ignorePressed = false;
        }
        // If mouse button is simply not down, ensure pressed state is false
        // (handles case where mouse was released outside)
        if (!leftMouseButtonDown && pressed) {
            pressed = false;
            // If mouse is still inside, trigger hover state again
            if (inside) buttonListener.entered(this);
        }
    }

    @Override
    public void render(Graphics g) {
        if (!active) return;

        // <<< Cast to Graphics2D and set Rendering Hints >>>
        Graphics2D g2d = (Graphics2D) g.create(); // Create a copy to not affect other rendering
        try {
            // Enable Anti-aliasing for smoother shapes
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Optional: Hint for better text rendering
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            Vector2i absolutePos = getAbsolutePosition();
            int x = absolutePos.x;
            int y = absolutePos.y;

            // 1. Draw Background or Image
            if (image != null) {
                g2d.drawImage(image, x, y, null);
            } else {
                g2d.setColor(this.backgroundColor); // Use current color (set by listener)
                g2d.fillRoundRect(x, y, size.x, size.y, arcWidth, arcHeight);
            }

            // 2. Draw Label (if it exists) - This part was missing/incorrect before
            // 2. Draw Label (if it exists)
            if (label != null && label.active && label.text != null && !label.text.isEmpty()) {
                Font currentFont = (label.getFont() != null) ? label.getFont() : g2d.getFont(); // Use label's font
                g2d.setFont(currentFont);
                FontMetrics fm = g2d.getFontMetrics(currentFont);

                // Calculate centered position for the text within the button bounds
                int textWidth = fm.stringWidth(label.text);
                int textHeight = fm.getHeight();
                int textAscent = fm.getAscent();

                int labelX = x + (size.x - textWidth) / 2;
                int labelY = y + (size.y - textHeight) / 2 + textAscent;

                // Set color and draw
                g2d.setColor(label.backgroundColor);
                g2d.drawString(label.text, labelX, labelY);
            }
        } finally {
            g2d.dispose(); // <<< Dispose the copied graphics context >>>
        }
    }
}

