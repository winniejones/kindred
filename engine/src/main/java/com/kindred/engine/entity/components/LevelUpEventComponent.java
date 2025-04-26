package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Optional: Temporary marker component added to an entity when it levels up.
 * Can be used to trigger other systems (like StatCalculationSystem, HealSystem).
 */
public class LevelUpEventComponent implements Component {
    public final int newLevel;

    public LevelUpEventComponent(int newLevel) {
        this.newLevel = newLevel;
    }
}
