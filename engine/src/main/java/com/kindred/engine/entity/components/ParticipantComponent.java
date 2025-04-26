package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

import java.util.*;

/**
 * Component attached to entities (like enemies) to track which other entities
 * (usually players) have participated in combat against it recently,
 * and how much damage each has dealt.
 * Used for distributing rewards like XP based on contribution.
 */
public class ParticipantComponent implements Component {

    // Map: Participant Entity ID -> Total Damage Dealt by that participant
    private final Map<Integer, Float> damageDealtByParticipant = new HashMap<>();

    // Optional: Could add timestamps per participant for damage decay or contribution window

    /**
     * Records damage dealt by a specific participant.
     * Adds the participant if they aren't already tracked.
     *
     * @param entityId The ID of the participant (e.g., player) dealing damage.
     * @param damageAmount The amount of damage dealt in this instance.
     */
    public void recordDamage(int entityId, float damageAmount) {
        if (damageAmount <= 0) return; // Don't record zero or negative damage

        // Get current damage for this participant, default to 0 if not present
        float currentDamage = damageDealtByParticipant.getOrDefault(entityId, 0f);
        // Add the new damage amount
        damageDealtByParticipant.put(entityId, currentDamage + damageAmount);
    }

    /**
     * Gets the total damage dealt by a specific participant.
     * @param entityId The ID of the participant.
     * @return The total damage dealt, or 0 if the participant hasn't dealt damage.
     */
    public float getDamageDealtBy(int entityId) {
        return damageDealtByParticipant.getOrDefault(entityId, 0f);
    }

    /**
     * Returns an unmodifiable view of the set of participant IDs who dealt damage.
     * @return An unmodifiable set of participant entity IDs.
     */
    public Set<Integer> getParticipants() {
        // Return the keys from the map
        return Collections.unmodifiableSet(damageDealtByParticipant.keySet());
    }

    /**
     * Returns an unmodifiable view of the damage map.
     * Be cautious about exposing the mutable Float values if direct modification is undesirable.
     * @return An unmodifiable map where keys are participant IDs and values are total damage dealt.
     */
    public Map<Integer, Float> getDamageMap() {
        return Collections.unmodifiableMap(damageDealtByParticipant);
    }


    /** Clears the list of participants and their damage. */
    public void clearParticipants() {
        damageDealtByParticipant.clear();
    }

    /** Checks if any participants have dealt damage. */
    public boolean isEmpty() {
        return damageDealtByParticipant.isEmpty();
    }

    /** Gets the number of unique participants who dealt damage. */
    public int getParticipantCount() {
        return damageDealtByParticipant.size();
    }

    /**
     * Calculates the total damage dealt by all recorded participants.
     * @return The sum of all damage values in the map.
     */
    public float getTotalDamageDealt() {
        float totalDamage = 0f;
        for (float damage : damageDealtByParticipant.values()) {
            totalDamage += damage;
        }
        return totalDamage;
    }
}
