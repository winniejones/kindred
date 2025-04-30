package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all top-level UI Panels in the game.
 * Responsible for updating and rendering the UI layer.
 */
@Slf4j
public class UIManager {

    private List<UIPanel> panels = new ArrayList<>();
    // Optional: Could add reference to EntityManager if UI needs game data
    // private EntityManager entityManager;

    public UIManager(/* EntityManager entityManager */) {
        // this.entityManager = entityManager;
    }

    /**
     * Adds a top-level panel to be managed.
     * @param panel The UIPanel to add.
     */
    public void addPanel(UIPanel panel) {
        if (panel != null) {
            panels.add(panel);
        }
    }

    /**
     * Updates all active panels managed by this UIManager.
     * Passes the current input state down to panels.
     * @param input The current InputState object.
     */
    public void update(InputState input, float deltaTime) { // <<< Accept InputState
        // Iterate in reverse to handle input for top-most panels first (optional)
        for (int i = panels.size() - 1; i >= 0; i--) {
            UIPanel panel = panels.get(i);
            if (panel.active) {
                panel.update(input, deltaTime); // <<< Pass input state down
            }
        }
    }

    /**
     * Renders all active panels managed by this UIManager.
     * @param g The Graphics context to draw on.
     */
    public void render(Graphics g) {
        // Render panels in the order they were added (can implement layers later)
        for (UIPanel panel : panels) {
            if (panel.active) {
                panel.render(g);
            }
        }
    }

    // --- Optional Helper Methods ---

    public void showPanel(UIPanel panelToShow) {
        if (panelToShow != null && panels.contains(panelToShow)) {
            log.debug("Showing panel: {}", panelToShow.getClass().getSimpleName());
            panelToShow.setActive(true);
        } else {
            log.warn("Attempted to show a panel not managed by UIManager or null panel.");
        }
    }
    public void hidePanel(UIPanel panelToHide) {
        if (panelToHide != null && panels.contains(panelToHide)) {
            log.debug("Hiding panel: {}", panelToHide.getClass().getSimpleName());
            panelToHide.setActive(false);
        } else {
            log.warn("Attempted to hide a panel not managed by UIManager or null panel.");
        }
    }
    public void togglePanel(UIPanel panelToToggle) {
        if (panelToToggle != null && panels.contains(panelToToggle)) {
            panelToToggle.setActive(!panelToToggle.active); // Flip the active state
            log.debug("Toggled panel {} visibility to: {}", panelToToggle.getClass().getSimpleName(), panelToToggle.active);
        } else {
            log.warn("Attempted to toggle a panel not managed by UIManager or null panel.");
        }
    }

    public boolean isMouseOverUI(int screenX, int screenY) {
        // Iterate panels in reverse order (top-most first)
        for (int i = panels.size() - 1; i >= 0; i--) {
            UIPanel panel = panels.get(i);
            if (panel.active) {
                // Check panel itself
                Vector2i panelPos = panel.getAbsolutePosition();
                if (screenX >= panelPos.x && screenX < panelPos.x + panel.size.x &&
                        screenY >= panelPos.y && screenY < panelPos.y + panel.size.y)
                {
                    // TODO: Need recursive check for components within the panel
                    // For now, just checking top-level panel bounds
                    return true; // Mouse is over this panel
                }
            }
        }
        return false; // Mouse is not over any active panel
    }

}

