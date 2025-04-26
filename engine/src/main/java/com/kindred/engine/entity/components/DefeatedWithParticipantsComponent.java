package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

import java.util.*;

/**
 * Temporary marker component added to a defeated entity.
 * Contains information needed by the ExperienceSystem to grant XP
 * to all participants.
 */
public class DefeatedWithParticipantsComponent implements Component {
    public final int xpValue;           // XP granted by the defeated entity
    public final Map<Integer, Float> damageMap;

    /**
     * Constructor.
     * @param xpValue The total XP value of the defeated entity.
     * @param damageMap A copy of the map containing damage dealt by each participant.
     */
    public DefeatedWithParticipantsComponent(int xpValue, Map<Integer, Float> damageMap) {
        this.xpValue = xpValue;
        // Store an immutable copy to prevent modification after creation
        this.damageMap = (damageMap != null) ? Collections.unmodifiableMap(new HashMap<>(damageMap)) : Collections.emptyMap();
    }

    /** Gets the set of participant IDs from the damage map keys. */
    public Set<Integer> getParticipantIds() {
        return damageMap.keySet(); // The keyset of the unmodifiable map is unmodifiable
    }

    /** Gets the total damage recorded in the map. */
    public float getTotalDamageDealt() {
        float total = 0f;
        for (float damage : damageMap.values()) {
            total += damage;
        }
        return total;
    }
}
