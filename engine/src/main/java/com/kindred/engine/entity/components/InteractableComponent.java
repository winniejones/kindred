package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Marker component indicating that an entity can be interacted with
 * by the player or other entities.
 */
public class InteractableComponent implements Component {
    // No data needed for basic interaction, but could hold interaction type, range, etc.
    public float interactionRange = 40f; // Example: Max distance in pixels to interact

    public InteractableComponent() {}

    public InteractableComponent(float range) {
        this.interactionRange = Math.max(1.0f, range); // Ensure positive range
    }
}
