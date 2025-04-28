package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.DeadComponent;
import com.kindred.engine.entity.components.InteractableComponent;
import com.kindred.engine.entity.components.InteractionAttemptComponent;
import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles interaction attempts between entities.
 * Looks for entities trying to interact and finds nearby interactable targets.
 */
@Slf4j
public class InteractionSystem implements System {

    private final EntityManager entityManager;

    public InteractionSystem(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null.");
        }
        this.entityManager = entityManager;
        log.info("InteractionSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        // Find entities attempting to interact (usually just the player)
        // Copy to list to avoid concurrent modification if removing component
        List<Integer> interactors = new ArrayList<>(entityManager.getEntitiesWith(InteractionAttemptComponent.class, PositionComponent.class));

        if (interactors.isEmpty()) {
            return; // No one is trying to interact
        }

        // Get all potential interactable targets
        // TODO: Optimize later with spatial query instead of checking all interactables
        Set<Integer> targets = entityManager.getEntitiesWith(InteractableComponent.class, PositionComponent.class);

        for (int interactorId : interactors) {
            // Check if interactor still valid
            if (!entityManager.isEntityActive(interactorId) || entityManager.hasComponent(interactorId, DeadComponent.class)) {
                entityManager.removeComponent(interactorId, InteractionAttemptComponent.class); // Clean up
                continue;
            }

            PositionComponent interactorPos = entityManager.getComponent(interactorId, PositionComponent.class);
            if (interactorPos == null) continue;

            InteractableComponent closestTargetComp = null;
            int closestTargetId = -1;
            float closestDistSq = Float.MAX_VALUE;

            // Find the closest interactable target within range
            for (int targetId : targets) {
                if (interactorId == targetId) continue; // Can't interact with self
                if (entityManager.hasComponent(targetId, DeadComponent.class)) continue; // Can't interact with dead

                PositionComponent targetPos = entityManager.getComponent(targetId, PositionComponent.class);
                InteractableComponent interactableComp = entityManager.getComponent(targetId, InteractableComponent.class);
                if (targetPos == null || interactableComp == null) continue;

                float dx = targetPos.x - interactorPos.x;
                float dy = targetPos.y - interactorPos.y;
                float distSq = dx * dx + dy * dy;
                float rangeSq = interactableComp.interactionRange * interactableComp.interactionRange;

                // Check if within range AND closer than the previous closest
                if (distSq <= rangeSq && distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestTargetId = targetId;
                    closestTargetComp = interactableComp;
                }
            }

            // If a target was found in range
            if (closestTargetId != -1) {
                log.info("Entity {} interacted with Entity {}!", interactorId, closestTargetId);

                // --- Trigger Interaction Logic ---
                // TODO: Replace log message with actual interaction (e.g., open dialogue UI, pick up item)
                // Example: Add an event component to the target
                // entityManager.addComponent(closestTargetId, new InteractionEventComponent(interactorId));

                // --- Optional: Make NPC face player ---
                // if (entityManager.hasComponent(closestTargetId, NPCComponent.class) && entityManager.hasComponent(closestTargetId, DirectionComponent.class)) {
                //     DirectionComponent targetDir = entityManager.getComponent(closestTargetId, DirectionComponent.class);
                //     // Calculate direction from target to interactor and set targetDir.facingDirection
                // }

            } else {
                log.trace("Entity {} interaction attempt found no target in range.", interactorId);
            }


            // Remove the attempt component regardless of success
            entityManager.removeComponent(interactorId, InteractionAttemptComponent.class);

        } // End loop through interactors
    } // End update()
}
