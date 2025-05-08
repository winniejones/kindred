package com.kindred.engine.ui.widgets;

import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.ui.*;
import com.kindred.engine.ui.panels.MinimapPanel;
import com.kindred.engine.ui.panels.PlayerStatsPanel;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

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

    private final int panelsStartY;   // Y just below the button bar
    private final int margin;         // reuse the sidebar margin
    private UIPanel lastOpenedPanel;  // null until one is opened

    private final Deque<UIPanel> stack = new ArrayDeque<>();
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
        this.margin = Const.MARGIN_2;
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
        currentY += Const.STATS_HEIGHT;

        // --- Button Bar Panel ---
        buttonBarPanel = new UIPanel(new Vector2i(margin, currentY), new Vector2i(contentW, Const.BAR_HEIGHT))
                .setBackgroundColor(Const.COLOR_BG_BTN_BAR);
        addComponent(buttonBarPanel);

        // --- Create Buttons (without final action yet) and Add to Button Bar ---
        int btnY = 0;
        // Create buttons with a placeholder action initially, store references
        skillsButton = createMenuButton("Skills", new Vector2i(margin, btnY), () -> {
        });
        optionsButton = createMenuButton("Options", new Vector2i(margin * 2 + Const.MENU_BTN_WIDTH, btnY), () -> {
        });
        buttonBarPanel.addComponent(skillsButton);
        buttonBarPanel.addComponent(optionsButton);
        currentY += Const.BAR_HEIGHT + margin;
        this.panelsStartY = currentY;

        // --- Create Hidden Skills Panel ---
        int headerHeight = 10;
        skillsPanel = new UIWindowPanel(
                new Vector2i(margin, currentY),
                new Vector2i(contentW, Const.SKILLS_HEIGHT),
                () -> skillsButton.setToggled(false)
        )
                .setColor(Const.COLOR_BG_SKILLS)
                .setActive(false)
                .setHeaderHeight(headerHeight)
                .setHeaderColor(Const.COLOR_STONE_800)
                .setTitleColor(Const.COLOR_TEXT_LIGHT)
                .setTitleFont(Const.FONT_SANS_BOLD_8)
                .setTitle("Skills");
        addComponent(skillsPanel);

        // --- Create Hidden Options Panel ---
        optionsPanel = new UIWindowPanel(
                new Vector2i(margin, currentY),
                new Vector2i(contentW, Const.OPTIONS_HEIGHT),
                () -> optionsButton.setToggled(false)
        )
                .setColor(Const.COLOR_BG_OPTIONS)
                .setActive(false)
                .setHeaderHeight(headerHeight)
                .setHeaderColor(Const.COLOR_STONE_800)
                .setTitleColor(Const.COLOR_TEXT_LIGHT)
                .setTitleFont(Const.FONT_SANS_BOLD_8)
                .setTitle("Options");
        addComponent(optionsPanel);

        stack.addLast(skillsPanel);
        stack.addLast(optionsPanel);

        skillsButton.setActionListener(this::toggleSkillsPanel);
        optionsButton.setActionListener(this::toggleOptionsPanel);

        layoutStack();
        // TODO: Add Inventory Panels below if needed, updating currentY
    }

    // --- Helper methods for creating common elements ---

    private UIButton createMenuButton(String text, Vector2i pos, Runnable action) {
        return new UIButton(pos, new Vector2i(Const.MENU_BTN_WIDTH, Const.MENU_BTN_HEIGHT), text, action::run)
                .setToggleable(true)
                .setFont(Const.FONT_BTN)
                .setColor(Const.COLOR_STONE_100)
                .setHoverColor(Const.COLOR_STONE_500)
                .setBaseColor(Const.COLOR_STONE_700)
                .setBackgroundColor(Const.COLOR_STONE_700);
        // TODO: Set hover/press colors using constants?
    }

    // Helper for close buttons, now takes an additional action for the main button
    private UIButton createCloseButton(UIPanel panelToClose, Runnable onMainButtonUntoggleAction) {
        Vector2i size = new Vector2i(Const.CLOSE_BTN_SIZE, Const.CLOSE_BTN_SIZE);
        Vector2i pos = new Vector2i(panelToClose.size.x - size.x - 3, 3);
        if (panelToClose instanceof UIWindowPanel) { // Adjust Y for header
            int outerBorderSize = ((UIWindowPanel) panelToClose).getOuterBorderSize();
            int headerHeight = ((UIWindowPanel) panelToClose).getHeaderHeight();
            int btnX = panelToClose.size.x - size.x - outerBorderSize - 3; // 3px padding from right border
            int btnY = outerBorderSize + (headerHeight - size.y) / 2; // Center vertically in header area
            pos = new Vector2i(btnX, btnY);
        }
        return new UIButton(pos, size, "X", () -> {
            panelToClose.setActive(false);
            bringToBack(panelToClose);
            if (onMainButtonUntoggleAction != null) {
                onMainButtonUntoggleAction.run(); // Untoggle the corresponding menu button
            }
        }).setColor(Const.COLOR_STONE_900).setFont(Const.FONT_SANS_BOLD_7);
    }

    // --- Public API for this Widget ---


    /** Toggles the visibility of the Skills panel. */
    public void toggleSkillsPanel() {
        boolean on = !skillsPanel.active;
        skillsPanel.setActive(on);
        if (on) bringToBack(skillsPanel);
        layoutStack();
    }

    /** Toggles the visibility of the Options panel. */
    public void toggleOptionsPanel() {
        boolean on = !optionsPanel.active;
        optionsPanel.setActive(on);
        if (on) bringToBack(optionsPanel);
        layoutStack();
    }

    private void bringToBack(UIPanel p) {
        stack.remove(p);             // O(1) in ArrayDeque
        stack.addLast(p);            // newest at the tail
    }

    /** Lay out everything that is currently active */
    private void layoutStack() {
        int y = panelsStartY;
        for (UIPanel p : stack) {
            if (p.active) {
                p.setPosition(margin, y);
                y += p.size.y + margin;
            }
        }
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
    @Override
    public void render(Graphics g) {
        super.render(g);                         // draws the button bar, minimap, etc.

        for (UIPanel p : stack) {                // our own draw order
            if (p.active) p.render(g);
        }
    }
}
