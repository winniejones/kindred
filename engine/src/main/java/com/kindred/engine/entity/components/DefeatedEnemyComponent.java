package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Temporary marker component added to a defeated entity.
 * Contains information needed by the ExperienceSystem to grant XP.
 */
public class DefeatedEnemyComponent implements Component {
    public final int xpValue;    // XP granted by the defeated enemy
    public final int killerId;   // Entity ID of the entity that landed the killing blow (or responsible)

    public DefeatedEnemyComponent(int xpValue, int killerId) {
        this.xpValue = xpValue;
        this.killerId = killerId;
    }
}
