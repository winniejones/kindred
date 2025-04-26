package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Component storing base attributes and potentially derived stats for an entity.
 */
public class StatsComponent implements Component {

    // --- Base Attributes ---
    // These are typically increased by leveling up or base character creation.
    public int strength = 10;
    public int dexterity = 10;
    public int intelligence = 10;
    public int vitality = 10;
    // Add other base stats as needed (e.g., wisdom, luck)

    // --- Derived Stats ---
    // These are calculated by systems based on base attributes, level, equipment, buffs etc.
    // They are often public but primarily *set* by systems, not directly modified elsewhere.
    public float attackPower = 0;    // Calculated damage bonus/multiplier
    public float defensePower = 0;   // Calculated damage reduction/mitigation
    public float maxHealthBonus = 0; // Bonus HP derived from vitality/level
    public float maxManaBonus = 0;   // Bonus MP derived from intelligence/level (if using mana)
    public float attackSpeed = 1.0f; // Attacks per second or delay multiplier
    public float movementSpeedModifier = 1.0f; // Multiplier for base movement speed
    // Add other derived stats (crit chance, haste, etc.)

    /**
     * Default constructor with base stats initialized to 10.
     */
    public StatsComponent() {}

    /**
     * Constructor allowing setting initial base stats.
     */
    public StatsComponent(int strength, int dexterity, int intelligence, int vitality) {
        this.strength = Math.max(1, strength); // Ensure minimum base stats
        this.dexterity = Math.max(1, dexterity);
        this.intelligence = Math.max(1, intelligence);
        this.vitality = Math.max(1, vitality);
        // Derived stats start at 0 or default values until calculated by a system
    }

    // Note: No update/logic here. This is purely a data container.
    // The StatCalculationSystem will read base stats and write derived stats.
}
