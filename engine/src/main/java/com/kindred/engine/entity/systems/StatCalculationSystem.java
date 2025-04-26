package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.ExperienceComponent;
import com.kindred.engine.entity.components.HealthComponent;
import com.kindred.engine.entity.components.LevelUpEventComponent;
import com.kindred.engine.entity.components.StatsComponent;
import com.kindred.engine.entity.core.EntityManager;
import lombok.extern.slf4j.Slf4j;

import com.kindred.engine.entity.core.System;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates derived stats based on base attributes, level, etc.
 * Can be triggered by events like LevelUpEventComponent.
 * Also updates dependent components like max health.
 */
@Slf4j
public class StatCalculationSystem implements System {

    private final EntityManager entityManager;

    public StatCalculationSystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        log.info("StatCalculationSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        // Process entities that just leveled up
        List<Integer> leveledUpEntities = new ArrayList<>(entityManager.getEntitiesWith(LevelUpEventComponent.class));

        for (int entityId : leveledUpEntities) {
            if (!entityManager.isEntityActive(entityId)) continue; // Check if still active

            LevelUpEventComponent levelEvent = entityManager.getComponent(entityId, LevelUpEventComponent.class);
            log.debug("Processing level up event for entity {} to level {}", entityId, levelEvent.newLevel);

            // --- Apply Level Up Bonuses (Example: Increase base stats) ---
            // This is one place to handle stat increases. Alternatively, ExperienceSystem could do it.
            if (entityManager.hasComponent(entityId, StatsComponent.class)) {
                StatsComponent stats = entityManager.getComponent(entityId, StatsComponent.class);
                // Example: +1 to each stat every level
                stats.strength += 1;
                stats.dexterity += 1;
                stats.intelligence += 1;
                stats.vitality += 1;
                log.debug("Entity {} base stats increased. STR:{}, DEX:{}, INT:{}, VIT:{}",
                        entityId, stats.strength, stats.dexterity, stats.intelligence, stats.vitality);
            }

            // --- Recalculate Stats for the entity that leveled up ---
            recalculateStats(entityId);

            // --- Optional: Heal on Level Up ---
            if (entityManager.hasComponent(entityId, HealthComponent.class)) {
                HealthComponent health = entityManager.getComponent(entityId, HealthComponent.class);
                health.currentHealth = health.maxHealth; // Heal to full
                log.debug("Entity {} healed to full health ({}) on level up.", entityId, health.maxHealth);
            }
            // TODO: Heal mana if applicable

            // Remove the event component after processing
            entityManager.removeComponent(entityId, LevelUpEventComponent.class);
        }

        // TODO: Decide if recalculation should happen at other times
        // (e.g., when equipment changes, buffs/debuffs applied/expired).
        // If so, trigger recalculateStats based on other event components or run periodically.
    }

    /**
     * Recalculates derived stats for a specific entity based on its
     * base stats, level, equipment, buffs, etc.
     * Updates dependent components like HealthComponent.maxHealth.
     *
     * @param entityId The ID of the entity whose stats need recalculation.
     */
    public void recalculateStats(int entityId) {
        if (!entityManager.isEntityActive(entityId)) return;

        // Get necessary components
        StatsComponent stats = entityManager.getComponent(entityId, StatsComponent.class);
        ExperienceComponent exp = entityManager.getComponent(entityId, ExperienceComponent.class); // Need level
        HealthComponent health = entityManager.getComponent(entityId, HealthComponent.class);
        // Get EquipmentComponent, BuffComponent etc. if they exist and affect stats

        if (stats == null) {
            log.warn("Attempted to recalculate stats for entity {} without StatsComponent.", entityId);
            return; // Cannot calculate without base stats
        }

        int level = (exp != null) ? exp.currentLevel : 1; // Default to level 1 if no Exp component

        // --- Calculate Derived Stats (Example Formulas - Adjust to your design!) ---
        // Attack Power: Based on Strength (for melee/physical) or Intelligence (for magic)
        // This might depend on the entity's class or equipped weapon type later.
        stats.attackPower = stats.strength * 1.5f; // Example: 1 STR = 1.5 AP

        // Defense Power: Based on Dexterity (for dodge/armor) or Vitality
        stats.defensePower = stats.dexterity * 0.5f + stats.vitality * 0.2f; // Example

        // Max Health Bonus: Based on Vitality and Level
        stats.maxHealthBonus = stats.vitality * 5f + level * 10f; // Example: 1 VIT = 5 HP, 1 Level = 10 HP

        // Max Mana Bonus: Based on Intelligence and Level
        stats.maxManaBonus = stats.intelligence * 7f + level * 5f; // Example

        // Attack Speed: Maybe influenced by Dexterity (higher dex = lower number = faster?)
        // Example: Base delay 1.0 seconds, reduced slightly by dex. Clamp minimum.
        stats.attackSpeed = Math.max(0.1f, 1.0f - (stats.dexterity - 10) * 0.01f);

        // Movement Speed Modifier: Maybe influenced by Dexterity
        stats.movementSpeedModifier = 1.0f + (stats.dexterity - 10) * 0.005f; // Small bonus/penalty


        // --- Update Dependent Components ---
        if (health != null) {
            float oldMaxHealth = health.maxHealth;
            // Assume a base health value + bonus from stats
            float baseHealth = 50; // TODO: Define base health based on class/race?
            health.maxHealth = baseHealth + stats.maxHealthBonus;
            // Ensure current health doesn't exceed new max health
            health.currentHealth = Math.min(health.currentHealth, health.maxHealth);
            if (health.maxHealth != oldMaxHealth) {
                log.debug("Entity {} max health updated: {} -> {}", entityId, oldMaxHealth, health.maxHealth);
            }
        }
        // TODO: Update ManaComponent.maxMana if applicable

        log.trace("Recalculated stats for entity {}: AP:{}, DEF:{}, HP+:{}, MP+:{}, AS:{}, MS:{}",
                entityId, stats.attackPower, stats.defensePower, stats.maxHealthBonus, stats.maxManaBonus,
                stats.attackSpeed, stats.movementSpeedModifier);
    }
}

