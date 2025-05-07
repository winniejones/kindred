package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import com.kindred.engine.ui.listener.UIActionListener;
import com.kindred.engine.ui.listener.UIButtonListener;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static com.kindred.engine.ui.UIRenderHelper.drawBorder;

/**
 * A clickable UI button component. Can display text or an image.
 * Triggers an action when clicked.
 */
@Slf4j
public class UIButton extends UIComponent {

    public Vector2i size;
    public UILabel label;
    private BufferedImage image = null;

    private Color colorOnHover;
    private Color baseColor;

    private UIActionListener actionListener;
    private UIButtonListener buttonListener;

    // State tracking
    private boolean inside = false;
    private boolean pressed = false;
    private boolean ignorePressed = false;
    private boolean toggled = false;
    private boolean toggleable = false;



    /**
     * Creates a button with text.
     */
    public UIButton(Vector2i position, Vector2i size, String text, UIActionListener actionListener) {
        this(position, size, text, Const.COLOR_TEXT_LIGHT, Const.COLOR_BG_BTN_HOVER, Const.COLOR_BG_BTN_DEFAULT, actionListener);
    }

    public UIButton(Vector2i position, Vector2i size, String text, Color textColor, Color hoverColor, Color baseColor, UIActionListener actionListener) {
        super(position);
        if (size == null || size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("Button size must be positive.");
        }
        this.size = size;
        this.actionListener = actionListener;

        // Create the label but DON'T add it to the panel here.
        // Position will be relative to the button's top-left (0,0) for rendering calculation.
        this.label = new UILabel(new Vector2i(0, 0), text).setActive(true);
        // We will calculate the centered position within the render method
        this.color = textColor;
        this.colorOnHover = hoverColor;
        this.baseColor = baseColor;
        this.backgroundColor = baseColor;
        initDefaultListener();
    }

    /**
     * Creates a button using an image.
     */
    public UIButton(Vector2i position, BufferedImage image, Color colorOnHover, UIActionListener actionListener) {
        super(position);
        if (image == null) {
            throw new IllegalArgumentException("Button image cannot be null");
        }
        this.size = new Vector2i(image.getWidth(), image.getHeight());
        this.actionListener = actionListener;
        this.image = image;
        this.label = null;
        this.colorOnHover = colorOnHover;
        this.baseColor = this.backgroundColor;
        initDefaultListener();
    }

    private void initDefaultListener() {
        this.buttonListener = new UIButtonListener();
    }

    public UIButton setTextColor(Color color) {
        if (label != null) {
            this.label.setColor(color);
        }
        return this;
    }

    public UIButton setHoverColor(Color color) { this.colorOnHover = color; return this; }
    public UIButton setBaseColor(Color color) { this.baseColor = color; return this; }
    @Override public UIButton setBackgroundColor(Color color) { super.setBackgroundColor(color); return this; }
    @Override public UIButton setBackgroundColor(int color) { super.setBackgroundColor(color); return this; }
    @Override public UIButton setColor(Color color) { super.setColor(color); return this; }
    @Override public UIButton setColor(int color) { super.setColor(color); return this; }
    @Override public UIButton setActive(boolean active) { super.setActive(active); return this; }
    @Override public UIButton setPosition(Vector2i position) { super.setPosition(position); return this; }
    @Override public UIButton setPosition(int x, int y) { super.setPosition(x, y); return this; }

    public UIButton setToggled(boolean toggled) {
        this.toggled = toggled;
        // Update visual state immediately if not currently being pressed by mouse
        return this;
    }
    public UIButton setToggleable(boolean toggleable) {
        this.toggleable = toggleable;
        return this;
    }
    public boolean isToggled() { return toggled; }

    /** Sets the size of the button using Vector2i (fluent). */
    public UIButton setSize(Vector2i size) {
        if (size != null && size.x > 0 && size.y > 0) { this.size = size; }
        return this;
    }

    /** Sets the size of the button using int dimensions (fluent). */
    public UIButton setSize(int width, int height) {
        if (width > 0 && height > 0) {
            if (this.size == null) { this.size = new Vector2i(width, height); }
            else { this.size.set(width, height); }
        }
        return this;
    }

    public void setActionListener(UIActionListener listener) {
        this.actionListener = (listener != null) ? listener : () -> {};
    }

    public UIButton setButtonListener(UIButtonListener listener) {
        this.buttonListener = (listener != null) ? listener : new UIButtonListener();
        return this;
    }

    public UIButton setText(String text) {
        if (label != null) {
            label.setText(text);
        } else if (image == null && text != null && !text.isEmpty()) {
            // If it was an image button, but now setting text, create a label
            label = new UILabel(new Vector2i(0, 0), text).setActive(true);
        }
        return this;
    }
    public UIButton setFont(Font font) {
        if (label != null) {
            label.setFont(font);
        }
        return this;
    }
    public UIButton setImage(BufferedImage image) { /* ... (same logic) ... */ return this; }

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
        onHover(currentlyInside, leftMouseButtonDown, leftMouseButtonPressed, leftMouseButtonReleased);

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

    private void onHover(boolean currentlyInside, boolean leftMouseButtonDown, boolean leftMouseButtonPressed, boolean leftMouseButtonReleased) {
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
                setToggled(!isToggled());
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
                buttonListener.exited(this);
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
            int w = size.x;
            int h = size.y;

            // 1. Draw Background or Image
            if (image != null) {
                g2d.drawImage(image, x, y, null);
            } else {
                g2d.setColor(this.backgroundColor); // Use current color (set by listener)
                g2d.fillRect(x, y, size.x, size.y);
            }

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
                g2d.setColor(this.color);
                g2d.drawString(label.text, labelX, labelY);
            }

            Color L1 = Const.COLOR_STONE_300; // Default light border
            Color L2 = Const.COLOR_STONE_900; // Default dark border
            if((this.toggleable && this.toggled) || pressed) {
                L1 = Const.COLOR_STONE_900; // Inset: dark on top/left
                L2 = Const.COLOR_STONE_300;
            }
            drawBorder(g2d, x, y, w, h, 1, L1, L2);
        } finally {
            g2d.dispose(); // <<< Dispose the copied graphics context >>>
        }
    }
}

