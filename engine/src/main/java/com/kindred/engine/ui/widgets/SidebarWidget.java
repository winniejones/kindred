package com.kindred.engine.ui.widgets;

import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.ui.*;

import java.awt.*;

/**
 * A self-contained UI widget representing the main game sidebar.
 * It creates and manages its child panels (Minimap, Stats, Buttons, etc.).
 */
public final class SidebarWidget extends UIPanel {

    // Child panels managed by this widget
    private final MinimapPanel minimapPanel;
    private final PlayerStatsPanel playerStatsPanel;
    private final UIPanel buttonBarPanel;
    private final UIPanel skillsPanel;
    private final UIPanel optionsPanel;

    private final UIButton skillsButton;
    private final UIButton optionsButton;
    // Add inventory panels if needed

    /**
     * Constructs the SidebarWidget.
     *
     * @param windowW Parent window width (used for positioning).
     * @param windowH Parent window height.
     * @param em EntityManager (passed to PlayerStatsPanel).
     * @param playerId Player's entity ID (passed to PlayerStatsPanel).
     */
    public SidebarWidget(
        int windowW, int windowH,
        EntityManager em, int playerId
    ) {
        // --- Initialize Sidebar Panel itself ---
        super(new Vector2i(windowW - Const.SIDEBAR_WIDTH, 0),
                new Vector2i(Const.SIDEBAR_WIDTH, windowH));
        setColor(Const.COLOR_BG_SIDEBAR);

        // --- Calculate layout values ---
        int margin = Const.MARGIN_2;
        int contentW = size.x - margin * 2;
        int currentY = margin;

        // --- Minimap Panel ---
        int minimapSize = contentW;
        minimapPanel = new MinimapPanel(new Vector2i(margin, currentY), new Vector2i(contentW, minimapSize));
        addComponent(minimapPanel);
        currentY += minimapSize + margin;

        // --- Player Stats Panel ---
        playerStatsPanel = new PlayerStatsPanel(new Vector2i(margin, currentY), new Vector2i(contentW, Const.STATS_HEIGHT), em, playerId);
        addComponent(playerStatsPanel);
        currentY += Const.STATS_HEIGHT + margin;

        // --- Button Bar Panel ---
        buttonBarPanel = new UIPanel(new Vector2i(margin, currentY), new Vector2i(contentW, Const.BAR_HEIGHT)).setColor(Const.COLOR_BG_BTN_BAR);
        addComponent(buttonBarPanel);

        // --- Create Buttons (without final action yet) and Add to Button Bar ---
        int btnY = 2;
        // Create buttons with a placeholder action initially, store references
        skillsButton = createMenuButton("Skills", new Vector2i(2, btnY), () -> {});
        optionsButton = createMenuButton("Options", new Vector2i(2   * 2 + Const.MENU_BTN_WIDTH, btnY), () -> {});
        buttonBarPanel.addComponent(skillsButton);
        buttonBarPanel.addComponent(optionsButton);
        currentY += Const.BAR_HEIGHT + margin;

        // --- Create Hidden Skills Panel ---
        skillsPanel = new UIPanel(new Vector2i(margin, currentY), new Vector2i(contentW, Const.SKILLS_HEIGHT)).setColor(Const.COLOR_BG_SKILLS).setActive(false);
        skillsPanel.addComponent(createCloseButton(skillsPanel));
        addComponent(skillsPanel);

        // --- Create Hidden Options Panel ---
        optionsPanel = new UIPanel(new Vector2i(margin, currentY), new Vector2i(contentW, Const.OPTIONS_HEIGHT)).setColor(Const.COLOR_BG_OPTIONS).setActive(false);
        optionsPanel.addComponent(createCloseButton(optionsPanel));
        addComponent(optionsPanel);

        // TODO: Add Inventory Panels below if needed, updating currentY
    }

    // --- Helper methods for creating common elements ---

    private UIButton createMenuButton(String text, Vector2i pos, Runnable action) {
        return new UIButton(pos, new Vector2i(Const.MENU_BTN_WIDTH, Const.MENU_BTN_HEIGHT), text, action::run)
                .setFont(Const.FONT_BTN)
                .setColor(Color.BLACK)
                .setBackgroundColor(Const.COLOR_BG_BTN_DEFAULT);
        // TODO: Set hover/press colors using constants?
    }

    private UIButton createCloseButton(UIPanel panelToClose) {
        Vector2i size = new Vector2i(Const.CLOSE_BTN_SIZE, Const.CLOSE_BTN_SIZE);
        // Position relative to top-right corner of the panel it's closing
        Vector2i pos = new Vector2i(panelToClose.size.x - size.x - 3, 3);
        return new UIButton(pos, size, "X", () -> panelToClose.setActive(false))
                .setColor(Const.COLOR_BG_CLOSE_BTN)
                .setFont(Const.FONT_CLOSE_BTN);
    }

    // --- Public API for this Widget ---

    /** Toggles the visibility of the Skills panel. */
    public void toggleSkillsPanel() {
        skillsPanel.setActive(!skillsPanel.active);
        // Optional: Hide options if skills shown
        if (skillsPanel.active) optionsPanel.setActive(false);
    }

    /** Toggles the visibility of the Options panel. */
    public void toggleOptionsPanel() {
        optionsPanel.setActive(!optionsPanel.active);
        // Optional: Hide skills if options shown
        if (optionsPanel.active) skillsPanel.setActive(false);
    }

    /** Updates the player ID the stats panel should track (if player changes). */
    public void setPlayer(int newPlayerId) {
        playerStatsPanel.setPlayerId(newPlayerId); // Assumes PlayerStatsPanel has this method
    }

    public void setSkillsButtonAction(Runnable action) {
        if (skillsButton != null && action != null) {
            skillsButton.setActionListener(action::run);
        }
    }
    public void setOptionsButtonAction(Runnable action) {
        if (optionsButton != null && action != null) {
            optionsButton.setActionListener(action::run);
        }
    }

    // Note: update() and render() are handled by the UIPanel base class
}
