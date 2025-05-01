package com.kindred.engine.ui.layout;

import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.ui.UIManager;
import com.kindred.engine.ui.widgets.ChatWidget;
import com.kindred.engine.ui.widgets.SidebarWidget;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory class responsible for building the standard game UI layout
 * by creating and composing the necessary UI widgets (Sidebar, Chat).
 * Provides a simplified interface for accessing UI elements.
 */
@Slf4j
public final class DefaultGameUILayout {

    private final UIManager uiManager;
    private final SidebarWidget sidebarWidget;
    private final ChatWidget chatWidget;

    // Private constructor - instances created via static build method
    private DefaultGameUILayout(UIManager uiManager, SidebarWidget sidebarWidget, ChatWidget chatWidget) {
        this.uiManager = uiManager;
        this.sidebarWidget = sidebarWidget;
        this.chatWidget = chatWidget;
    }

    /**
     * Builds the default game UI layout.
     * Creates the Sidebar and Chat widgets and adds them to the UIManager.
     *
     * @param uiManager The UIManager instance.
     * @param windowW The game window width.
     * @param windowH The game window height.
     * @param entityManager The EntityManager (needed for stats panel).
     * @param playerEntityId The player's entity ID (needed for stats panel).
     * @return An initialized DefaultGameUILayout instance.
     */
    public static DefaultGameUILayout build(UIManager uiManager, int windowW, int windowH,
                                            EntityManager entityManager, int playerEntityId) {
        log.info("Building Default Game UI Layout...");

        // 1. Create Sidebar Widget first (constructor no longer takes Runnables)
        SidebarWidget sidebar = new SidebarWidget(windowW, windowH, entityManager, playerEntityId);

        // 2. Create Chat widget
        ChatWidget chat = new ChatWidget(windowW, windowH);

        // 3. Add top-level widgets to the UIManager
        uiManager.addPanel(sidebar);
        uiManager.addPanel(chat);

        // 4. Set button actions AFTER sidebar is created, using lambdas that capture the 'sidebar' instance
        //    (Assumes SidebarWidget has methods like setSkillsButtonAction)
        sidebar.setSkillsButtonAction(sidebar::toggleSkillsPanel);
        sidebar.setOptionsButtonAction(sidebar::toggleOptionsPanel);


        log.info("Default Game UI Layout built successfully.");
        // 5. Return the layout object which holds references to the main widgets
        return new DefaultGameUILayout(uiManager, sidebar, chat);
    }

    // --- Public Facade Methods ---
    // Expose methods from child widgets that the rest of the game might need to call

    /** Adds a line of text to the chat display. */
    public void addChatLine(String message) {
        if (chatWidget != null) {
            chatWidget.addLine(message);
        }
    }

    /** Checks if the chat input currently has focus. */
    public boolean isChatInputFocused() {
        return (chatWidget != null) && chatWidget.isInputFocused();
    }

    /** Gives focus to the chat input. */
    public void focusChatInput() {
        if (chatWidget != null) chatWidget.focusInput();
    }

    /** Removes focus from the chat input. */
    public void unfocusChatInput() {
        if (chatWidget != null) chatWidget.unfocusInput();
    }

    /** Gets text submitted from chat input and clears it. */
    public String getSubmittedChatTextAndClear() {
        return (chatWidget != null) ? chatWidget.getSubmittedTextAndClear() : null;
    }

    /** Passes key events to the chat widget if needed (e.g., for focus handling). */
    public void handleChatKeyPress(java.awt.event.KeyEvent event) {
        if (chatWidget != null) chatWidget.handleKeyPress(event);
    }


    /** Updates the player ID tracked by the stats panel. */
    public void updatePlayerStatsTarget(int newPlayerId) {
        if (sidebarWidget != null) {
            sidebarWidget.setPlayer(newPlayerId);
        }
    }

    // Add other methods as needed to interact with UI elements,
    // e.g., updateInventorySlots(), setMinimapData() etc.
}
