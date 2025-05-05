package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.KeyEvent;

import static com.kindred.engine.ui.UIRenderHelper.drawBorder;

/**
 * A UI component for single-line text input.
 * Needs focus management and keyboard event handling from outside.
 */
@Slf4j
public class UITextInput extends UIComponent {

    private StringBuilder text = new StringBuilder();
    public Vector2i size;
    private Font font;
    private boolean focused = false;
    private int maxLength = 50; // Example max length
    private String submittedText = null; // Stores text submitted via Enter

    private Color backgroundOnFocusColor = Const.COLOR_STONE_800;
    private Color borderLightColor = Const.COLOR_STONE_400;
    private Color borderDarkColor = Const.COLOR_STONE_700;
    private int innerBorderSize = 1;

    // Cursor blinking state (simple example)
    private boolean showCursor = true;
    private float cursorBlinkTimer = 0f;
    private final float cursorBlinkRate = 0.5f;

    public UITextInput(Vector2i position, Vector2i size) {
        super(position);
        if (size == null || size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("UITextInput size must be positive.");
        }
        this.size = size;
        this.font = new Font("Arial", Font.PLAIN, 8);
        this.color = Const.COLOR_STONE_100; // Text color
        this.backgroundColor = Const.COLOR_STONE_600; // Background color
    }

    public UITextInput setSize(Vector2i size) {
        this.size = size;
        return this;
    }

    public UITextInput setSize(int x, int y) {
        if (this.size == null) {
            this.size = new Vector2i(x, y);
        } else {
            this.size.set(x, y);
        }
        return this;
    }

    public UITextInput setFont(Font font) {
        this.font = font;
        return this;
    }

    public UITextInput setMaxLength(int maxLength) {
        this.maxLength = Math.max(1, maxLength);
        return this;
    }

    public String getText() {
        return text.toString();
    }

    public UITextInput setText(String newText) {
        if (newText == null) {
            text = new StringBuilder();
        } else {
            text = new StringBuilder(newText.substring(0, Math.min(newText.length(), maxLength)));
        }
        return this;
    }

    public UITextInput setFocus(boolean focused) {
        if (this.focused != focused) {
            log.debug("Text input focus changed to: {}", focused);
        }
        this.focused = focused;
        if (focused) {
            showCursor = true; // Make sure cursor is visible when focus gained
            cursorBlinkTimer = 0f;
        }
        return this;
    }

    public UITextInput setInnerBorderSize(int size) { this.innerBorderSize = Math.max(0, size); return this; }
    public UITextInput setBorderColors(Color light, Color dark) { this.borderLightColor = light; this.borderDarkColor = dark; return this; }

    public boolean isFocused() {
        return focused;
    }

    /**
     * Handles a key press event IF this input field has focus.
     * To be called by the main input handling logic (e.g., in GameMain or InputSystem).
     * @param event The KeyEvent from the KeyListener.
     */
    public void handleKeyPress(KeyEvent event) {
        if (!active || !focused) return;

        int keyCode = event.getKeyCode();
        char keyChar = event.getKeyChar();

        synchronized (text) { // Synchronize modifications to text
            if (keyCode == KeyEvent.VK_BACK_SPACE) {
                if (text.length() > 0) {
                    text.deleteCharAt(text.length() - 1);
                }
            } else if (keyCode == KeyEvent.VK_ENTER) {
                // Submit the text (e.g., send chat message)
                submittedText = text.toString();
                log.info("Text Input Submitted: '{}'", submittedText);
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                setFocus(false);
            } else if (keyChar != KeyEvent.CHAR_UNDEFINED
                && !Character.isISOControl(keyChar)
                && keyCode != KeyEvent.VK_SHIFT
                && keyCode != KeyEvent.VK_CONTROL
                && keyCode != KeyEvent.VK_ALT
                && keyCode != KeyEvent.VK_META
                && text.length() < maxLength
            ) {
                // Append printable characters if within length limit
                text.append(keyChar);
            }
        }
        // Make cursor visible immediately after typing
        showCursor = true;
        cursorBlinkTimer = 0f;
    }

    /**
     * Gets the text that was submitted by pressing Enter (if any)
     * and clears the internal submitted text flag.
     * @return The submitted text, or null if nothing was submitted since last call.
     */
    public String getSubmittedTextAndClear() {
        String submitted = submittedText;
        submittedText = null; // Clear after retrieval
        if (submitted != null) {
            text.setLength(0); // Clear input field now
        }
        return submitted;
    }

    // --- Internal Logic ---
    private void deriveBorderColors(Color base) {
        if (base == null) {
            this.borderLightColor = Color.LIGHT_GRAY;
            this.borderDarkColor = Color.DARK_GRAY;
        } else {
            // Simple brighter/darker might not give the exact gray scale look
            // Use fixed shades for consistency?
            this.borderLightColor = Const.COLOR_STONE_300; // Example light border
            this.borderDarkColor = Const.COLOR_STONE_900;  // Example dark border
            // Or derive:
            // this.borderLightColor = base.brighter();
            // this.borderDarkColor = base.darker().darker(); // Make darker more pronounced
        }
    }

    @Override
    public UITextInput setBackgroundColor(Color color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public UITextInput setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public UITextInput setColor(Color color) {
        super.setColor(color);
        return this;
    }

    @Override
    public UITextInput setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public UITextInput setActive(boolean active) {
        super.setActive(active);
        return this;
    }

    @Override
    public UITextInput setPosition(Vector2i position) {
        super.setPosition(position);
        return this;
    }

    @Override
    public UITextInput setPosition(int x, int y) {
        super.setPosition(x, y);
        return this;
    }

    @Override
    public void update(InputState input, float deltaTime) {
        if (!active) return;

        // --- New: focus-on-click logic ---
        if (input.isButtonPressed(InputState.MOUSE_LEFT) ) {
            Point m = input.getMousePosition();
            Vector2i abs = getAbsolutePosition();
            boolean insideX = m.x >= abs.x && m.x <= abs.x + size.x;
            boolean insideY = m.y >= abs.y && m.y <= abs.y + size.y;
            setFocus(insideX && insideY);
        }

        // Update cursor blink state if focused
        if (focused) {
            // TODO: Use deltaTime passed from game loop for accurate timing
            cursorBlinkTimer += deltaTime;
            if (cursorBlinkTimer >= cursorBlinkRate) {
                showCursor = !showCursor;
                cursorBlinkTimer -= cursorBlinkRate; // Subtract rate, don't just reset to 0
            }
        } else {
            showCursor = false; // No cursor if not focused
        }
    }


    @Override
    public void render(Graphics g) {
        if (!active) return;

        Vector2i absolutePos = getAbsolutePosition();
        int x = absolutePos.x;
        int y = absolutePos.y;
        int w = size.x;
        int h = size.y;

        // Store original settings
        Color originalColor = g.getColor();
        Font originalFont = g.getFont();
        Rectangle originalClip = g.getClipBounds();

        try {
            // 1. Draw Background
            if(focused) {
                g.setColor(this.backgroundOnFocusColor);
            } else {
                g.setColor(this.backgroundColor != null ? this.backgroundColor : Color.WHITE);
            }
            g.fillRect(x, y, w, h);

            // 2. Draw Inset Border (Dark on Top/Left, Light on Bottom/Right)
            if (innerBorderSize > 0) {
                // Ensure border colors are set
                if (borderLightColor == null || borderDarkColor == null) deriveBorderColors(this.backgroundColor);

                drawBorder(g, x, y, w, h, innerBorderSize, borderDarkColor, borderLightColor);
            }

            // 3. Prepare for Text/Cursor (inside the border)
            int padding = innerBorderSize + 2; // Padding inside border
            int contentX = x + padding;
            int contentY = y + innerBorderSize; // Text baseline relative to top border
            int contentW = w - padding * 2;
            int contentH = h - innerBorderSize * 2;

            // Set clip bounds to content area
            g.setClip(contentX, contentY, contentW, contentH);

            // 4. Draw Text
            g.setFont(this.font);
            g.setColor(this.color);
            FontMetrics fm = g.getFontMetrics();
            // Vertically center text within the available content height
            int textY = y + innerBorderSize + (contentH - fm.getHeight()) / 2 + fm.getAscent();

            String currentText = "";
            synchronized(text) { // Synchronize access while rendering
                currentText = text.toString();
                g.drawString(currentText, contentX, textY);
            }
            log.trace("Rendering TextInput: AbsPos=({}, {}), Text='{}', DrawPos=({}, {})", x, y, currentText, contentX, textY);

            // 5. Draw Cursor
            if (focused && showCursor) {
                int cursorX = contentX + fm.stringWidth(currentText); // Position after text
                // Ensure cursor stays within clip bounds
                if (cursorX < contentX + contentW) {
                    int cursorY1 = textY - fm.getAscent() + 1;
                    int cursorY2 = textY + fm.getDescent();
                    g.setColor(this.color); // Use text color for cursor
                    g.drawLine(cursorX, cursorY1, cursorX, cursorY2);
                    log.trace("Drawing cursor at ({}, {}) to ({}, {})", cursorX, cursorY1, cursorX, cursorY2);
                }
            }

        } finally {
            // Restore original settings
            g.setColor(originalColor);
            g.setFont(originalFont);
            g.setClip(originalClip);
        }
    }
}
