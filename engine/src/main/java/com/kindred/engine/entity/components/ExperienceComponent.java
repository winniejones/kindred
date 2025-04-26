package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Component attached to entities that can gain experience and level up (e.g., Player).
 */
public class ExperienceComponent implements Component {

    public int currentLevel = 1;
    public long currentXP = 0;
    public long xpToNextLevel = 100; // Initial value, increases with level

    /** Default constructor, starts at level 1. */
    public ExperienceComponent() {}

    /**
     * Constructor allowing setting initial values.
     * @param startLevel The starting level.
     * @param startXP Current XP within that level.
     * @param startXpToNext XP needed for the first level up from startLevel.
     */
    public ExperienceComponent(int startLevel, long startXP, long startXpToNext) {
        this.currentLevel = Math.max(1, startLevel);
        this.xpToNextLevel = Math.max(1, startXpToNext); // Ensure positive threshold
        this.currentXP = Math.max(0, Math.min(startXP, this.xpToNextLevel - 1)); // Clamp XP
    }

    /**
     * Calculates the XP needed for the next level up based on the current level.
     * Example formula: increases by 50% each level (adjust as needed).
     * @param currentLevel The level the entity is currently at.
     * @return The total XP required to reach the *next* level.
     */
    public static long calculateXpForNextLevel(int currentLevel) {
        if (currentLevel <= 0) return 100; // Base case
        // Example: Simple exponential growth (adjust formula for desired progression curve)
        // Level 1 needs 100, Level 2 needs 150, Level 3 needs 225, etc.
        return (long) (100 * Math.pow(1.5, currentLevel - 1));
        // Alternative: Linear growth
        // return 100 + (currentLevel - 1) * 50;
    }
}
