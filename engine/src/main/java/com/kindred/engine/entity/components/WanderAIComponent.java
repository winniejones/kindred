package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

import java.util.Random;

public class WanderAIComponent implements Component {
    /** Possible states for the wandering AI. */
    public enum AIState {
        IDLE,      // Waiting before deciding where to move next
        WANDERING  // Moving towards the target destination
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
    /** Movement speed for wandering. */
    public final float wanderSpeed;


    // --- State Variables ---
    /** Current state of the AI (IDLE or WANDERING). */
    public AIState currentState;
    /** Timer counting down during the IDLE state (in seconds). */
    public float idleTimer;
    /** The current target X coordinate the entity is moving towards. */
    public int targetX;
    /** The current target Y coordinate the entity is moving towards. */
    public int targetY;

    // Random number generator for picking targets and idle times
    // Making it non-static ensures different entities don't always sync up
    private transient Random random = new Random(); // transient: prevent serialization if needed

    /**
     * Constructor for WanderAIComponent.
     *
     * @param startX The initial X coordinate, center of the wander area.
     * @param startY The initial Y coordinate, center of the wander area.
     * @param wanderRadius The maximum distance from the start point to wander.
     * @param minIdleTime Minimum idle time between wanders (seconds).
     * @param maxIdleTime Maximum idle time between wanders (seconds).
     * @param wanderSpeed The speed at which the entity moves when wandering.
     */
    public WanderAIComponent(int startX, int startY, float wanderRadius, float minIdleTime, float maxIdleTime, float wanderSpeed) {
        if (wanderRadius <= 0 || minIdleTime < 0 || maxIdleTime < minIdleTime || wanderSpeed <= 0) {
            throw new IllegalArgumentException("Invalid parameters for WanderAIComponent.");
        }
        this.startX = startX;
        this.startY = startY;
        this.wanderRadius = wanderRadius;
        this.minIdleTime = minIdleTime;
        this.maxIdleTime = maxIdleTime;
        this.wanderSpeed = wanderSpeed;

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
        // System.out.println("Entity reset idle timer to: " + idleTimer); // Debug
    }

    /** Picks a new random target position within the wander radius. */
    public void pickNewWanderTarget() {
        if (random == null) random = new Random();

        // Generate random angle and distance
        double angle = random.nextDouble() * 2.0 * Math.PI; // 0 to 2*PI
        double distance = random.nextDouble() * wanderRadius; // 0 to wanderRadius

        // Calculate new target relative to start position
        this.targetX = startX + (int)(Math.cos(angle) * distance);
        this.targetY = startY + (int)(Math.sin(angle) * distance);
        // System.out.println("Entity picked new target: (" + targetX + ", " + targetY + ")"); // Debug
    }
}
