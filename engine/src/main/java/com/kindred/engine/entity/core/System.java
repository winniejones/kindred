package com.kindred.engine.entity.core;

/**
 * Base interface for all Systems in the ECS.
 * Defines the contract for systems that process entities and components based on game logic.
 */
public interface System {

    /**
     * Updates the system's logic for one frame or simulation tick.
     * This method is typically called once per update cycle by the main game loop.
     *
     * @param deltaTime The time elapsed since the last update call, usually in seconds.
     * This allows for calculations that are independent of the frame rate
     * (e.g., movement speed, timers).
     */
    void update();

    // --- Optional Common Methods ---
    // You might add other methods here later if many systems need them,
    // though often they are not strictly necessary for a simple ECS.

    /**
     * Optional: Called once when the system is initialized or added to the engine.
     * Useful if a system needs to perform setup tasks (e.g., find specific entities, pre-calculate data).
     */
    // default void init() {
    //     // Default implementation does nothing
    // }

    /**
     * Optional: Called once when the system is being shut down or removed from the engine.
     * Useful for releasing resources or performing cleanup.
     */
    // default void dispose() {
    //     // Default implementation does nothing
    // }

}