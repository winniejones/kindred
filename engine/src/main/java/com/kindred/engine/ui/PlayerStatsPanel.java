package com.kindred.engine.ui;

import com.kindred.engine.entity.components.ExperienceComponent;
import com.kindred.engine.entity.components.HealthComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.input.InputState;
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
    private final int playerEntityId; // Store the player's entity ID

    // References to the labels within this panel
    private UILabel healthLabel;
    private UILabel levelLabel;
    private UILabel xpLabel;
    // Add more labels for other stats (Strength, etc.) as needed
    private UIProgressBar healthBar; // <<< Added Health Bar
    private UIProgressBar xpBar;

    /**
     * Creates the Player Stats Panel.
     * @param position Position relative to its parent (likely the sidebar).
     * @param size Dimensions of the panel.
     * @param entityManager Reference to the EntityManager to query components.
     * @param playerEntityId The entity ID of the player.
     */
    public PlayerStatsPanel(Vector2i position, Vector2i size, EntityManager entityManager, int playerEntityId) {
        super(position, size); // Call UIPanel constructor
        this.entityManager = entityManager;
        this.playerEntityId = playerEntityId;

        // Set a background color (optional)
        setColor(new Color(70, 70, 90, 210)); // Bluish gray from example

        // Create and add the labels and bars
        createElements();
    }

    private void createElements() {
        int barHeight = 5;
        int labelYOffset = -2; // Offset labels slightly above bars
        int barX = 10;
        int barWidth = size.x - 20; // Width of bars (panel width - margins)
        int currentY = 5; // Starting Y position for elements

        // Health Label and bar
        healthBar = new UIProgressBar(new Vector2i(barX, currentY), new Vector2i(barWidth, barHeight));
        healthBar.setForegroundColor(Color.RED); // Health is usually red
        healthBar.setBackgroundColor(new Color(38, 38, 38)); // Dark red background
        this.addComponent(healthBar);

        currentY += barHeight;
        healthLabel = new UILabel(new Vector2i(barX, currentY), "hp: --- / ---"); // Initial placeholder text
        healthLabel.setColor(Color.WHITE);
        healthLabel.setFont(new Font("Arial", Font.BOLD, 10));
        this.addComponent(healthLabel); // Add to this panel

        currentY += 15; // Move down for next section

        // --- Experience ---
        xpBar = new UIProgressBar(new Vector2i(barX, currentY), new Vector2i(barWidth, barHeight));
        xpBar.setForegroundColor(Color.WHITE);
        xpBar.setBackgroundColor(new Color(38, 38, 38)); // Dark yellow background
        this.addComponent(xpBar);

        currentY += barHeight;

        xpLabel = new UILabel(new Vector2i(barX, currentY), "exp: --- / ---");
        xpLabel.setColor(Color.WHITE);
        xpLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        this.addComponent(xpLabel);

        currentY += 15; // Move down for the bar

        // Level Label
        levelLabel = new UILabel(new Vector2i(barX, currentY), "level: ---");
        levelLabel.setColor(Color.WHITE);
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        this.addComponent(levelLabel);

        // TODO: Add labels for Strength, Dexterity, etc.
    }

    /**
     * Updates the text of the labels by querying the player's current components.
     * @param input The current InputState (not used directly here but part of the update signature).
     */
    @Override
    public void update(InputState input) {
        if (!active || !entityManager.isEntityActive(playerEntityId)) {
            // If panel inactive or player doesn't exist, maybe clear labels or show defaults
            healthLabel.setText("HP: N/A");
            healthBar.setProgress(0.0);
            levelLabel.setText("Level: N/A");
            xpLabel.setText("XP: N/A");
            xpBar.setProgress(0.0);
            return;
        }

        // --- Get Player Components ---
        HealthComponent health = entityManager.getComponent(playerEntityId, HealthComponent.class);
        ExperienceComponent experience = entityManager.getComponent(playerEntityId, ExperienceComponent.class);
        // StatsComponent stats = entityManager.getComponent(playerEntityId, StatsComponent.class); // Get stats if needed

        // --- Update Labels ---
        if (health != null) {
            // Format health nicely (e.g., integer values)
            healthLabel.setText(String.format("hp: %d / %d", (int)health.currentHealth, (int)health.maxHealth));
            healthBar.setProgress(health.getHealthPercentage()); // Use helper method
        } else {
            healthLabel.setText("HP: ???");
            healthBar.setProgress(0.0);
        }

        if (experience != null) {
            levelLabel.setText(String.format("level: %d", experience.currentLevel));
            // Format XP with commas for readability
            xpLabel.setText(String.format(Locale.US, "exp: %,d / %,d", experience.currentXP, experience.xpToNextLevel));
            // Calculate XP progress percentage for the bar
            double xpProgress = (experience.xpToNextLevel > 0) ? (double)experience.currentXP / experience.xpToNextLevel : 0.0;
            xpBar.setProgress(xpProgress);
        } else {
            levelLabel.setText("Level: ???");
            xpLabel.setText("XP: ???");
            xpBar.setProgress(0.0);
        }

        // TODO: Update other stat labels using data from StatsComponent if added

        // Call super.update AFTER updating labels if panels need base update logic,
        // but UIPanel's base update currently only handles children, which we did manually.
        super.update(input);
    }

    // Override the no-arg update as well if needed, though ideally it's not called
    @Override
    @Deprecated
    public void update() {
        update(new InputState());
    }
}
