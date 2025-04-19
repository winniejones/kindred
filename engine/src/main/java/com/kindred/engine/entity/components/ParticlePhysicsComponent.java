package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Component holding physics-related state specifically for particles,
 * including Z-axis position/velocity for simple gravity and bouncing effects.
 */
public class ParticlePhysicsComponent implements Component {

    /** Current height/position on the Z-axis. */
    public float z;
    /** Current velocity on the Z-axis. Positive is typically up. */
    public float vz;

    // --- Configurable Physics Properties ---
    /** Acceleration due to gravity (pixels/sec^2). Applied downwards (reduces vz). */
    public float gravity;
    /** Factor by which Z velocity is multiplied on ground bounce (e.g., -0.55 for reversal and damping). */
    public float bounceDamping;
    /** Factor by which X/Y velocity is multiplied when bouncing on ground (e.g., 0.4 for friction). */
    public float groundFriction;
    /** Factor by which velocity components are multiplied on wall collision (e.g., -0.5). */
    public float wallDamping;


    /**
     * Default constructor with typical physics values.
     * Starts slightly above ground with a small upward velocity.
     */
    public ParticlePhysicsComponent() {
        this.z = 1.0f; // Start slightly above ground
        this.vz = 1.0f + (float)(Math.random() * 2.0f); // Initial small upward pop (pixels/update assuming ~60ups) -> convert to pixels/sec
        // Convert initial vz assuming it was per-update (~60UPS) in old code to per-second
        this.vz *= 60.0f; // Adjust this factor based on your actual update rate if needed

        // Default physics values (adjust these!)
        // Gravity: Need a value in pixels/sec^2. 0.1 pixels/update^2 at 60UPS is roughly 0.1*60*60 = 360 pixels/sec^2 downwards.
        this.gravity = 360.0f;
        this.bounceDamping = -0.55f; // Matches old code
        this.groundFriction = 0.4f;  // Matches old code
        this.wallDamping = -0.5f;    // Matches old code
    }

    /**
     * Constructor with customizable physics values.
     */
    public ParticlePhysicsComponent(float initialZ, float initialVz, float gravity, float bounceDamping, float groundFriction, float wallDamping) {
        this.z = initialZ;
        this.vz = initialVz; // Assume this is already pixels/sec
        this.gravity = gravity;
        this.bounceDamping = bounceDamping;
        this.groundFriction = groundFriction;
        this.wallDamping = wallDamping;
    }
}

