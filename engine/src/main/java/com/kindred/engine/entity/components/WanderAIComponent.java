package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

import java.util.Random;

/**
 * Component holding state for simple AI behavior including wandering and attacking.
 * Entities with this component will randomly pick points within a radius
 * of their starting position and move towards them, pausing between movements.
 * If a player comes within aggroRadius, they will switch to ATTACKING state.
 */
public class WanderAIComponent implements Component {

    /** Possible states for the AI. */
    public enum AIState {
        IDLE,      // Waiting before deciding where to move next
        WANDERING, // Moving towards the random wander target destination
        ATTACKING  // Detected player, moving towards/attacking player
    }

    // --- Configuration ---
    /** The starting position (X) around which the entity will wander. */
    public final int startX;
    /** The starting position (Y) around which the entity will wander. */
    public final int startY;
    /** The maximum distance (in pixels) from the start position the entity will wander. */
    public final float wanderRadius;
    /** Minimum time (in seconds) to stay idle before wandering again. */
    public final float minIdleTime;
    /** Maximum time (in seconds) to stay idle before wandering again. */
    public final float maxIdleTime;
    /** Movement speed for wandering/attacking (can be overridden by specific states). */
    public final float moveSpeed;
    /** Distance (in pixels) within which the AI will detect and attack the player. */
    public final float aggroRadius;


    // --- State Variables ---
    /** Current state of the AI (IDLE, WANDERING, or ATTACKING). */
    public AIState currentState;
    /** Timer counting down during the IDLE state (in seconds). */
    public float idleTimer;
    /** The current target X coordinate (either wander target or player position). */
    public int targetX;
    /** The current target Y coordinate (either wander target or player position). */
    public int targetY;

    // Random number generator for picking targets and idle times
    private transient Random random = new Random(); // transient: prevent serialization if needed

    /**
     * Constructor for WanderAIComponent.
     *
     * @param startX The initial X coordinate, center of the wander area.
     * @param startY The initial Y coordinate, center of the wander area.
     * @param wanderRadius The maximum distance from the start point to wander.
     * @param minIdleTime Minimum idle time between wanders (seconds).
     * @param maxIdleTime Maximum idle time between wanders (seconds).
     * @param moveSpeed The speed at which the entity moves when wandering or attacking.
     * @param aggroRadius Distance within which to detect and attack the player.
     */
    public WanderAIComponent(int startX, int startY, float wanderRadius, float minIdleTime, float maxIdleTime, float moveSpeed, float aggroRadius) {
        if (wanderRadius <= 0 || minIdleTime < 0 || maxIdleTime < minIdleTime || moveSpeed <= 0 || aggroRadius <= 0) {
            throw new IllegalArgumentException("Invalid parameters for WanderAIComponent.");
        }
        this.startX = startX;
        this.startY = startY;
        this.wanderRadius = wanderRadius;
        this.minIdleTime = minIdleTime;
        this.maxIdleTime = maxIdleTime;
        this.moveSpeed = moveSpeed; // Renamed from wanderSpeed for clarity
        this.aggroRadius = aggroRadius;

        // Initialize state
        this.currentState = AIState.IDLE;
        resetIdleTimer(); // Set initial idle time
        // Set initial target to current position to avoid immediate movement
        this.targetX = startX;
        this.targetY = startY;
    }

    /** Resets the idle timer to a random duration within the defined min/max. */
    public void resetIdleTimer() {
         if (random == null) random = new Random(); // Re-initialize if deserialized
        this.idleTimer = minIdleTime + random.nextFloat() * (maxIdleTime - minIdleTime);
    }

     /** Picks a new random target position within the wander radius. */
     public void pickNewWanderTarget() {
         if (random == null) random = new Random();
         double angle = random.nextDouble() * 2.0 * Math.PI;
         double distance = random.nextDouble() * wanderRadius;
         this.targetX = startX + (int)(Math.cos(angle) * distance);
         this.targetY = startY + (int)(Math.sin(angle) * distance);
     }

    // Note: Getters/Setters can be added if direct field access is not preferred.
}
