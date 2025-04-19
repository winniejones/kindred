package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class HealthComponent implements Component {
    /** The entity's current health points. */
    public float currentHealth;

    /** The entity's maximum possible health points. */
    public float maxHealth;

    public HealthComponent (float maxHealth) {
        if (maxHealth <= 0) {
            // Log warning or throw exception for invalid max health
            System.err.println("Warning: Max health must be positive. Setting to 1.");
            this.maxHealth = 1.0f;
        } else {
            this.maxHealth = maxHealth;
        }
        // Start with full health
        this.currentHealth = this.maxHealth;
    }

    /**
     * Constructor allowing different starting current health.
     * Clamps currentHealth between 0 and maxHealth.
     * Ensures maxHealth is positive.
     *
     * @param currentHealth The starting health points (will be clamped).
     * @param maxHealth The maximum health points (must be > 0).
     */
    public HealthComponent(float currentHealth, float maxHealth) {
        if (maxHealth <= 0) {
            System.err.println("Warning: Max health must be positive. Setting to 1.");
            this.maxHealth = 1.0f;
        } else {
            this.maxHealth = maxHealth;
        }
        // Clamp current health to be between 0 and maxHealth
        this.currentHealth = Math.max(0, Math.min(currentHealth, this.maxHealth));
    }

    // --- Optional Helper Methods ---

    /**
     * Checks if the entity is currently considered "alive" (health > 0).
     * @return true if currentHealth is greater than 0, false otherwise.
     */
    public boolean isAlive() {
        return this.currentHealth > 0;
    }

    /**
     * Applies damage to the entity, reducing current health.
     * Clamps health at 0.
     * @param damageAmount The amount of damage to take (should be positive).
     */
    public void takeDamage(float damageAmount) {
        if (damageAmount > 0) {
            this.currentHealth = Math.max(0, this.currentHealth - damageAmount);
        }
    }

    /**
     * Heals the entity, increasing current health up to the maximum.
     * @param healAmount The amount to heal (should be positive).
     */
    public void heal(float healAmount) {
        if (healAmount > 0) {
            this.currentHealth = Math.min(this.maxHealth, this.currentHealth + healAmount);
        }
    }

    /**
     * Gets the health percentage (0.0 to 1.0).
     * Useful for drawing health bars.
     * @return The current health as a fraction of maximum health.
     */
    public float getHealthPercentage() {
        if (maxHealth <= 0) return 0; // Avoid division by zero
        return currentHealth / maxHealth;
    }
}
