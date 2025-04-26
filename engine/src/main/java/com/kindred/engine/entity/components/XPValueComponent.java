package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Component attached to entities that grant experience points when defeated.
 */
public class XPValueComponent implements Component {
    public int xpValue; // How much XP this entity grants

    public XPValueComponent(int xpValue) {
        this.xpValue = Math.max(0, xpValue); // Ensure non-negative XP
    }
}
