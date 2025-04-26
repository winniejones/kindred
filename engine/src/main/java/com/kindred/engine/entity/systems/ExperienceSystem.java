package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles experience gain and level progression for entities.
 * Processes DefeatedEnemyComponent to grant XP and triggers level ups.
 */
@Slf4j
public class ExperienceSystem implements System {

    private final EntityManager entityManager;

    public ExperienceSystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        log.info("ExperienceSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        // Query for entities defeated this frame that have participant data
        List<Integer> defeatedEntities = new ArrayList<>(entityManager.getEntitiesWith(DefeatedWithParticipantsComponent.class));

        for (int defeatedEntityId : defeatedEntities) {
            if (!entityManager.isEntityActive(defeatedEntityId) || !entityManager.hasComponent(defeatedEntityId, DefeatedWithParticipantsComponent.class)) {
                continue;
            }

            DefeatedWithParticipantsComponent defeatInfo = entityManager.getComponent(defeatedEntityId, DefeatedWithParticipantsComponent.class);
            int totalXp = defeatInfo.xpValue;
            Map<Integer, Float> damageMap = defeatInfo.damageMap; // Get the damage map

            if (damageMap == null || damageMap.isEmpty() || totalXp <= 0) {
                log.trace("Skipping XP distribution for entity {}: No participants or zero XP.", defeatedEntityId);
                entityManager.removeComponent(defeatedEntityId, DefeatedWithParticipantsComponent.class); // Clean up component
                entityManager.removeComponent(defeatedEntityId, ParticipantComponent.class); // Also remove tracker
                continue;
            }

            // --- Calculate Total Damage Dealt by Participants ---
            float totalDamageDealt = 0f;
            for (float damage : damageMap.values()) {
                totalDamageDealt += damage;
            }

            if (totalDamageDealt <= 0) {
                 log.warn("Total damage dealt for entity {} was zero or negative. Skipping XP distribution.", defeatedEntityId);
                 entityManager.removeComponent(defeatedEntityId, DefeatedWithParticipantsComponent.class);
                 entityManager.removeComponent(defeatedEntityId, ParticipantComponent.class);
                 continue;
            }

            log.debug("Distributing {} total XP based on damage contribution (Total Damage: {}) for kill of entity {}.",
                      totalXp, totalDamageDealt, defeatedEntityId);

            // --- Distribute XP based on Damage Ratio ---
            for (Map.Entry<Integer, Float> entry : damageMap.entrySet()) {
                int participantId = entry.getKey();
                float damageDealtByParticipant = entry.getValue();

                // Calculate share ratio
                float damageRatio = damageDealtByParticipant / totalDamageDealt;
                // Calculate XP share (use Math.round for fairness with integer XP)
                int xpShare = Math.max(1, Math.round(totalXp * damageRatio)); // Grant at least 1 XP if they dealt damage

                // Grant XP to the participant
                if (entityManager.isEntityActive(participantId) && entityManager.hasComponent(participantId, ExperienceComponent.class)) {
                    ExperienceComponent expComp = entityManager.getComponent(participantId, ExperienceComponent.class);

                    expComp.currentXP += xpShare;
                    log.debug("Entity {} gained {} XP ({}% damage share). Total XP: {}/{}",
                              participantId, xpShare, String.format("%.1f", damageRatio * 100), expComp.currentXP, expComp.xpToNextLevel);

                    // Check for level up
                    boolean leveledUp = false;
                    while (expComp.currentXP >= expComp.xpToNextLevel) {
                        leveledUp = true;
                        expComp.currentXP -= expComp.xpToNextLevel;
                        expComp.currentLevel++;
                        long newXpThreshold = ExperienceComponent.calculateXpForNextLevel(expComp.currentLevel);
                        expComp.xpToNextLevel = newXpThreshold;
                        log.info("Entity {} leveled up to Level {}! XP: {}/{}", participantId, expComp.currentLevel, expComp.currentXP, expComp.xpToNextLevel);
                        entityManager.addComponent(participantId, new LevelUpEventComponent(expComp.currentLevel));
                    }
                } else {
                    log.warn("Participant entity {} not found or cannot gain XP.", participantId);
                }
            } // End loop through participants

            // Remove the processed event component AND the participant tracker from the defeated entity
            entityManager.removeComponent(defeatedEntityId, DefeatedWithParticipantsComponent.class);
            entityManager.removeComponent(defeatedEntityId, ParticipantComponent.class); // <<< Remove tracker here

        } // End loop through defeated entities
    } // End update()
}
