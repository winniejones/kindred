package com.kindred.engine.ui.widgets;

import com.kindred.engine.entity.components.ExperienceComponent;
import com.kindred.engine.entity.components.HealthComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.input.InputState;
import com.kindred.engine.ui.*;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.Locale;

/**
 * A specific UIPanel designed to display the player's current stats
 * like Health, Level, and Experience.
 */
@Slf4j
public class PlayerStatsPanel extends UIPanel {

    private final EntityManager entityManager;
    private int playerEntityId; // Store the player's entity ID

    // References to the labels within this panel
    private UILabel healthLabel;
    private UILabel levelLabel;
    private UILabel xpLabel;
    private UIProgressBar healthBar; // <<< Added Health Bar
    private UIProgressBar xpBar;
    // Add more labels for other stats (Strength, etc.) as needed

    /**
     * Creates the Player Stats Panel.
     * @param position Position relative to its parent (likely the sidebar).
     * @param size Dimensions of the panel.
     * @param entityManager Reference to the EntityManager to query components.
     * @param playerEntityId The entity ID of the player.
     */
    public PlayerStatsPanel(Vector2i position, Vector2i size, EntityManager entityManager, int playerEntityId) {
        super(position, size);
        if (entityManager == null) throw new IllegalArgumentException("EntityManager cannot be null");
        this.entityManager = entityManager;
        this.playerEntityId = playerEntityId;

        // Set a background color (optional)
        setBackgroundColor(Const.COLOR_BG_STATS); // Bluish gray from example

        // Create and add the labels and bars
        createElements();

        // Perform initial update to populate labels/bars
        updateLabelsAndBars();
    }

    public PlayerStatsPanel setSize(int x, int y) {
        this.setSize(x, y);
        return this;
    }

    private void createElements() {
        int barPosX = Const.MARGIN_2;
        int currentY = Const.MARGIN_2; // Starting Y position for elements

        // Health Label and bar
        // Label
        healthLabel = createStatLabel(barPosX, 1, "HP: --- / ---");
        this.addComponent(healthLabel);

        currentY += Const.MARGIN_10;
        // Bar
        healthBar = createHPBar(barPosX, currentY);
        this.addComponent(healthBar);

        currentY += healthBar.getHeight()+Const.MARGIN_2; // Move down for next section

        // --- Experience ---
        // Label
        xpLabel = createStatLabel(barPosX, currentY, "XP: --- / ---");
        this.addComponent(xpLabel);

        currentY += Const.MARGIN_10;
        // Bar
        xpBar = createXPStatBar(barPosX, currentY);
        this.addComponent(xpBar);

        currentY += xpBar.getHeight()+Const.MARGIN_2; // Move down for the bar

        // Level Label
        levelLabel = createStatLabel(barPosX, currentY, "Level: ---");
        this.addComponent(levelLabel);

        // TODO: Add labels for Strength, Dexterity, etc.
    }

    private UIProgressBar createBar(int barX, int currentY, Color backgroundColor, Color foregroundColor) {
        int barHeight = Const.STATUS_BAR_HEIGHT;
        int barWidth = size.x - Const.MARGIN_2*2; // Width of bars (panel width - margins)
        return new UIProgressBar(new Vector2i(barX, currentY), new Vector2i(barWidth, barHeight))
                .setForegroundColor(foregroundColor)
                .setBackgroundColor(backgroundColor);
    }

    private UIProgressBar createHPBar(int barX, int currentY) {
        return this.createBar(barX, currentY, Const.COLOR_BG_HEALTH_BAR, Const.COLOR_FG_HEALTH_BAR );
    }

    private UIProgressBar createManaBar(int barX, int currentY) {
        return this.createBar(barX, currentY, Const.COLOR_BG_MANA_BAR, Const.COLOR_FG_MANA_BAR );
    }

    private UIProgressBar createXPStatBar(int barX, int currentY) {
        return this.createBar(barX, currentY, Const.COLOR_BG_STATUS_XP_BAR, Const.COLOR_FG_STATUS_XP_BAR );
    }

    private UILabel createStatLabel(int barX, int currentY, String labelText) {
        return new UILabel(new Vector2i(barX, currentY), labelText)
                .setBackgroundColor(Color.WHITE)
                .setFont(Const.FONT_STATS_LABEL);
    }

    /**
     * Sets the ID of the player entity this panel should display stats for.
     * @param newPlayerId The new entity ID for the player.
     */
    public void setPlayerId(int newPlayerId) {
        if (this.playerEntityId != newPlayerId) {
            log.info("PlayerStatsPanel updating target entity ID from {} to {}", this.playerEntityId, newPlayerId);
            this.playerEntityId = newPlayerId;
            // Immediately update display when player changes
            updateLabelsAndBars();
        }
    }

    /**
     * Updates the text of the labels by querying the player's current components.
     * @param input The current InputState (not used directly here but part of the update signature).
     */
    @Override
    public void update(InputState input, float deltaTime) {
        if (!active) return;

        updateLabelsAndBars();

        // Call super.update AFTER updating labels if panels need base update logic,
        // but UIPanel's base update currently only handles children, which we did manually.
        super.update(input, deltaTime);
    }

    /**
     * Updates the text/progress of the labels/bars by querying the player's current components.
     * Separated from the main update loop logic.
     */
    private void updateLabelsAndBars() {
        if (!active) return;

        if (playerEntityId == -1  || !entityManager.isEntityActive(playerEntityId)) {
            // If panel inactive or player doesn't exist, maybe clear labels or show defaults
            healthLabel.setText("HP: N/A");
            healthBar.setProgress(0.0);
            levelLabel.setText("Level: N/A");
            xpLabel.setText("XP: N/A");
            xpBar.setProgress(0.0);
        } else {
            // --- Get Player Components ---
            HealthComponent health = entityManager.getComponent(playerEntityId, HealthComponent.class);
            ExperienceComponent experience = entityManager.getComponent(playerEntityId, ExperienceComponent.class);
            // StatsComponent stats = entityManager.getComponent(playerEntityId, StatsComponent.class); // Get stats if needed

            // --- Update Labels ---
            updateHealth(health);
            updateXP(experience);
        }
    }

    private void updateXP(ExperienceComponent experience) {
        if (experience != null) {
            levelLabel.setText(String.format("Level: %d", experience.currentLevel));
            // Format XP with commas for readability
            xpLabel.setText(String.format(Locale.US, "XP: %,d / %,d", experience.currentXP, experience.xpToNextLevel));
            // Calculate XP progress percentage for the bar
            double xpProgress = (experience.xpToNextLevel > 0) ? (double) experience.currentXP / experience.xpToNextLevel : 0.0;
            xpBar.setProgress(xpProgress);
        } else {
            levelLabel.setText("Level: ???");
            xpLabel.setText("XP: ???");
            xpBar.setProgress(0.0);
        }
    }

    private void updateHealth(HealthComponent health) {
        if (health != null) {
            healthLabel.setText(String.format("HP: %d / %d", (int) health.currentHealth, (int) health.maxHealth));
            healthBar.setProgress(health.getHealthPercentage()); // Use helper method
        } else {
            healthLabel.setText("HP: ???");
            healthBar.setProgress(0.0);
        }
    }
}
