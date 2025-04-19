package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * A temporary marker component added to an entity when it takes damage.
 * Used by systems like VisualEffectsSystem or RenderSystem to trigger
 * visual feedback like flashing. Includes a timer for the effect duration.
 */
public class TookDamageComponent implements Component {

    /** Time remaining for the visual effect (e.g., flash) in seconds. */
    public float effectTimer;

    /** How long the effect should initially last (in seconds). */
    public final float initialDuration;

    /**
     * Creates a TookDamageComponent.
     * @param durationSeconds How long the visual effect should last.
     */
    public TookDamageComponent(float durationSeconds) {
        this.initialDuration = Math.max(0.01f, durationSeconds); // Ensure positive duration
        this.effectTimer = this.initialDuration;
    }
}
