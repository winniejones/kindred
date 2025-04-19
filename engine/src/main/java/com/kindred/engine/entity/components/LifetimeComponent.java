package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Component managing the lifetime of an entity (like a particle).
 * The entity will be destroyed when remainingLifetime reaches zero.
 */
public class LifetimeComponent implements Component {
    public float remainingLifetime; // Time left in seconds
    public final float initialLifetime; // Initial duration in seconds

    public LifetimeComponent(float lifetimeSeconds) {
        this.initialLifetime = Math.max(0.01f, lifetimeSeconds); // Store initial duration (at least a tiny bit)
        this.remainingLifetime = this.initialLifetime; // Start timer at full duration
    }
}
