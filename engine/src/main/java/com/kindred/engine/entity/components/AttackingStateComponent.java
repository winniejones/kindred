package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Temporary component added to an entity when it starts an attack animation/action.
 * Used to track which targets have already been hit during the current "swing" or
 * attack sequence to prevent a single animation from hitting the same target multiple times.
 */
public class AttackingStateComponent implements Component {
    // Stores entity IDs that have been hit during the current attack swing
    public Set<Integer> hitTargetsThisSwing;

    public AttackingStateComponent() {
        this.hitTargetsThisSwing = new HashSet<>();
    }

    /**
     * Adds a target entity's ID to the set of entities hit during this swing.
     * @param targetId The ID of the entity that was hit.
     */
    public void addHitTargetThisSwing(int targetId) {
        hitTargetsThisSwing.add(targetId);
    }

    /**
     * Checks if a specific target entity has already been hit during this swing.
     * @param targetId The ID of the target entity to check.
     * @return true if the target has already been hit in this swing, false otherwise.
     */
    public boolean hasHitTargetThisSwing(int targetId) {
        return hitTargetsThisSwing.contains(targetId);
    }

    /**
     * Clears the set of hit targets. Should be called at the beginning of a new attack swing.
     */
    public void clearHitTargets() {
        hitTargetsThisSwing.clear();
    }
}
