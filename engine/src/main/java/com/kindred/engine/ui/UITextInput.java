package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * A UI component for single-line text input.
 * Needs focus management and keyboard event handling from outside.
 */
@Slf4j
public class UITextInput extends UIComponent {

    private StringBuilder text = new StringBuilder();
    public Vector2i size; // Explicit size required
    private Font font;
    private boolean focused = false;
    private int maxLength = 50; // Example max length
    private String submittedText = null; // Stores text submitted via Enter

    // Cursor blinking state (simple example)
    private boolean showCursor = true;
    private float cursorBlinkTimer = 0f;
    private final float cursorBlinkRate = 0.5f; // Blink interval in seconds

    public UITextInput(Vector2i position, Vector2i size) {
        super(position);
        if (size == null || size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("UITextInput size must be positive.");
        }
        this.size = size;
        this.font = new Font("Arial", Font.PLAIN, 12);
        this.color = Color.BLACK; // Text color
        this.backgroundColor = Color.WHITE; // Background color
    }

    public void setFont(Font font) { this.font = font; }
    public void setMaxLength(int maxLength) { this.maxLength = Math.max(1, maxLength); }
    public String getText() { return text.toString(); }
    public void setText(String newText) {
        if (newText == null) {
            text = new StringBuilder();
        } else {
            text = new StringBuilder(newText.substring(0, Math.min(newText.length(), maxLength)));
        }
    }

    public void setFocus(boolean focused) {
        if (this.focused != focused) {
            log.debug("Text input focus changed to: {}", focused);
        }
        this.focused = focused;
        if (focused) {
            showCursor = true; // Make sure cursor is visible when focus gained
            cursorBlinkTimer = 0f;
        }
    }
    public boolean isFocused() { return focused; }

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
                // Clear the input field after submission
                // text.setLength(0); // Clear immediately
                // Or handle clearing after processing submittedText elsewhere
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                // Optional: Lose focus on Escape?
                // setFocus(false);
            } else if (!Character.isISOControl(keyChar) && text.length() < maxLength) {
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


    @Override
    public void update(InputState input, float deltaTime) {
        if (!active) return;

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

        // Store original settings
        Color originalColor = g.getColor();
        Font originalFont = g.getFont();
        Rectangle originalClip = g.getClipBounds();

        try {
            // Draw background
            g.setColor(this.backgroundColor != null ? this.backgroundColor : Color.WHITE);
            g.fillRect(x, y, size.x, size.y);

            // Set clip bounds
            g.setClip(x + 2, y, size.x - 4, size.y); // Add padding to clip

            // Draw text
            g.setFont(this.font);
            g.setColor(this.color);
            FontMetrics fm = g.getFontMetrics();
            int textY = y + (size.y - fm.getHeight()) / 2 + fm.getAscent(); // Center text vertically
            int textX = x + 2; // Small padding from left

            String currentText = "";
            synchronized(text) { // Synchronize access while rendering
                currentText = text.toString(); // Get text safely
                g.drawString(currentText, textX, textY);
            }
             // <<< Log text rendering details >>>
             log.trace("Rendering TextInput: AbsPos=({}, {}), Size=({}, {}), Text='{}', DrawPos=({}, {})", x, y, size.x, size.y, currentText, textX, textY);


            // Draw cursor if focused and blinking allows
            if (focused && showCursor) {
                int cursorX = textX + fm.stringWidth(currentText); // Position after text
                int cursorY1 = textY - fm.getAscent() + 1;
                int cursorY2 = textY + fm.getDescent();
                g.setColor(this.color); // Use text color for cursor
                g.drawLine(cursorX, cursorY1, cursorX, cursorY2);
                // <<< Log cursor drawing >>>
                log.trace("Drawing cursor at ({}, {}) to ({}, {})", cursorX, cursorY1, cursorX, cursorY2);
            }

        } finally {
            // Restore original settings
            g.setColor(originalColor);
            g.setFont(originalFont);
            g.setClip(originalClip);
        }
    }
}
