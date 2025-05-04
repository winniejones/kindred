package com.kindred.engine.ui.widgets;

import com.kindred.engine.input.InputState;
import com.kindred.engine.ui.*;
import lombok.extern.slf4j.Slf4j;

/**
 * A self-contained UI widget for the game chat area.
 * Contains the text display area and the text input field.
 */
@Slf4j
public class ChatWidget extends UIPanel {

    private final UIChatDisplay chatArea; // Renamed from UIChatDisplay if needed
    private final UITextInput chatInput;

    /**
     * Constructs the ChatWidget.
     * @param windowW Parent window width.
     * @param windowH Parent window height.
     */
    public ChatWidget(int windowW, int windowH) {
        // Position bottom-left, width adjusted for sidebar
        super(new Vector2i(0, windowH - Const.CHAT_HEIGHT),
                new Vector2i(windowW - Const.SIDEBAR_WIDTH, Const.CHAT_HEIGHT));
        setColor(Const.COLOR_BG_CHAT); // Use constant

        // --- Calculate child dimensions ---
        int margin = Const.MARGIN_2;
        int contentWidth = (windowW - Const.SIDEBAR_WIDTH) - margin * 2;
        int inputHeight = Const.CHAT_INPUT_HEIGHT;
        int areaHeight = (Const.CHAT_HEIGHT - Const.CHAT_INPUT_HEIGHT - Const.MARGIN_2) - inputHeight - margin * 3; // Space for area above input

        // --- Create and Add Chat Display Area ---
        chatArea = new UIChatDisplay(
                new Vector2i(margin, margin), // Relative to chat panel
                new Vector2i(contentWidth, areaHeight)
        );
        chatArea.setMaxLines(15); // Example line count
        // chatArea.setFont(Const.DEFAULT_FONT); // Set font if needed
        addComponent(chatArea);

        // --- Create and Add Chat Input Field ---
        chatInput = new UITextInput(
                new Vector2i(margin, Const.CHAT_HEIGHT - Const.CHAT_INPUT_HEIGHT - Const.MARGIN_2), // Below chat area
                new Vector2i(contentWidth, inputHeight)
        );
        // chatInput.setFont(Const.DEFAULT_FONT); // Set font if needed
        addComponent(chatInput);
    }

    // --- Public API for this Widget ---

    /** Adds a line of text to the chat display area. */
    public void addLine(String message) {
        chatArea.addLine(message);
    }

    /** Checks if the chat input field currently has focus. */
    public boolean isInputFocused() {
        return chatInput.isFocused();
    }

    /** Gives focus to the chat input field. */
    public void focusInput() {
        chatInput.setFocus(true);
    }

    /** Removes focus from the chat input field. */
    public void unfocusInput() {
        chatInput.setFocus(false);
    }

    /** Gets text submitted via Enter from the input field and clears it. */
    public String getSubmittedTextAndClear() {
        return chatInput.getSubmittedTextAndClear();
    }

    /** Passes key events to the input field if it's focused. */
    public void handleKeyPress(java.awt.event.KeyEvent event) {
        if (isInputFocused()) {
            chatInput.handleKeyPress(event);
        }
    }

    // Update method calls super.update which updates children
    @Override
    public void update(InputState input, float deltaTime) {
        if (!active) return;
        super.update(input, deltaTime); // Calls update on chatArea and chatInput
    }

    // Render method calls super.render which renders children
    // @Override
    // public void render(Graphics g) {
    //     if (!active) return;
    //     super.render(g); // Draws background and calls render on children
    // }
}
