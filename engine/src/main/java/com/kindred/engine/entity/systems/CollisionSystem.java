package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.level.Level;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle; // Use Rectangle for AABB checks
import java.util.Set; // Import Set

/**
 * System responsible for handling collision detection between entities
 * (with Position, Velocity, and Collider components) and the solid tiles
 * in the game Level. It adjusts the entity's velocity component if a
 * collision is detected, preventing movement into solid areas.
 */
@Slf4j
public class CollisionSystem implements System {
    private final EntityManager entityManager;
    private final Level level;

    // Reusable Rectangle objects for AABB checks to avoid constant allocation
    private final Rectangle boundsA = new Rectangle();
    private final Rectangle boundsB = new Rectangle();
    private final Rectangle projectedBoundsA = new Rectangle();
    private final Rectangle projectedBoundsB = new Rectangle();


    public CollisionSystem(EntityManager entityManager, Level level) {
        if (entityManager == null || level == null) throw new IllegalArgumentException("Dependencies cannot be null.");
        this.entityManager = entityManager;
        this.level = level;
        log.info("CollisionSystem initialized.");
    }

    /**
     * Updates all relevant entities, checking for collisions and adjusting velocity.
     * This should typically be called after input handling and before the MovementSystem.
     */
    @Override
    public void update(float deltaTime) {
        update(); // Call no-arg version for now, deltaTime not strictly needed for resolution logic
    }

    public void update() {
        int tileSize = level.getTileSize();
        if (tileSize <= 0) {
            log.error("CollisionSystem: Invalid tile size in Level. Skipping collision checks.");
            return; // Cannot perform checks with invalid tile size
        }

        // --- Gather Collidable Entities ---
        List<Integer> collidableEntities = getCollidableEntities();
        // ----------------------------------

        // --- Phase 1: Entity-vs-Tile Collision ---
        checkEntityVsTileCollision(collidableEntities, tileSize);

        // --- Phase 2: Entity-vs-Entity Collision ---
        checkEntityVsEntityCollision(collidableEntities);
    } // End update()

    private void checkEntityVsEntityCollision(List<Integer> collidableEntities) {
        // (Resolves collisions between entities, using velocity potentially modified by tiles)
        // Brute-force N^2 check (Optimize later with spatial partitioning if needed)
        for (int i = 0; i < collidableEntities.size(); i++) {
            int entityA = collidableEntities.get(i);
            // Get potentially updated components for A after tile collision pass
            PositionComponent posA = entityManager.getComponent(entityA, PositionComponent.class);
            VelocityComponent velA = entityManager.getComponent(entityA, VelocityComponent.class);
            ColliderComponent colA = entityManager.getComponent(entityA, ColliderComponent.class);

            if (posA == null || velA == null || colA == null) continue; // Skip if components missing

            // Get A's current velocity for this phase's checks
            int velAX = velA.vx;
            int velAY = velA.vy;

            for (int j = i + 1; j < collidableEntities.size(); j++) {
                int entityB = collidableEntities.get(j);
                PositionComponent posB = entityManager.getComponent(entityB, PositionComponent.class);
                VelocityComponent velB = entityManager.getComponent(entityB, VelocityComponent.class);
                ColliderComponent colB = entityManager.getComponent(entityB, ColliderComponent.class);

                if (posB == null || velB == null || colB == null) continue; // Skip if B missing components

                // --- Debug Logging ---
                // boolean isAPlayer = entityManager.hasComponent(entityA, PlayerComponent.class);
                // boolean isBPlayer = entityManager.hasComponent(entityB, PlayerComponent.class);
                // boolean isANPC = entityManager.hasComponent(entityA, NPCComponent.class);
                // boolean isBNPC = entityManager.hasComponent(entityB, NPCComponent.class);
                // // Log only if player is involved with NPC for targeted debugging
                // if ((isAPlayer && isBNPC) || (isANPC && isBPlayer)) {
                //      log.debug("Checking EvE: A={} (NPC:{}) vs B={} (NPC:{})", entityA, isANPC, entityB, isBNPC);
                // }
                // -------------------
                boolean isAPlayer = entityManager.hasComponent(entityA, PlayerComponent.class);
                boolean isBPlayer = entityManager.hasComponent(entityB, PlayerComponent.class);
                boolean isANPC = entityManager.hasComponent(entityA, NPCComponent.class);
                boolean isBNPC = entityManager.hasComponent(entityB, NPCComponent.class);
                if ((isAPlayer && isBNPC) || (isANPC && isBPlayer)) {
                    log.trace("Checking EvE: A={} vs B={}", entityA, entityB);
                }

                // --- AABB Bounds Calculation ---
                boundsA.setBounds(posA.x + colA.offsetX, posA.y + colA.offsetY, colA.hitboxWidth, colA.hitboxHeight);
                boundsB.setBounds(posB.x + colB.offsetX, posB.y + colB.offsetY, colB.hitboxWidth, colB.hitboxHeight);


                // --- Check for CURRENT Overlap (Optional but can help resolve sticking) ---
                // if (boundsA.intersects(boundsB)) {
                //     log.warn("Entities {} and {} are already overlapping!", entityA, entityB);
                //     // Apply separation logic here if needed
                // }

                 // --- Check B moving into A (Reciprocal check needed for simultaneous movement) ---
                 // This part is more complex if both can move. A simpler approach for now
                 // assumes the previous checks sufficiently stop overlaps by preventing
                 // entities from moving *into* currently occupied space.
                 // A full physics response would handle momentum, mass, pushing etc.

                // --- Predictive Check & Response ---
                // Check A moving horizontally into B's current position
                if (velAX != 0) {
                    projectedBoundsA.setBounds(boundsA.x + velAX, boundsA.y, boundsA.width, boundsA.height);
                    if (projectedBoundsA.intersects(boundsB)) {
                        if ((isAPlayer && isBNPC) || (isANPC && isBPlayer))
                            log.debug("EvE Stop: A ({}) stops X-move due to B ({})", entityA, entityB);
                        velA.vx = 0; // Stop A's horizontal move
                        velAX = 0;   // Update local copy for subsequent checks this frame
                    }
                }
                // Check B moving horizontally into A's current position
                if (velB.vx != 0) {
                    projectedBoundsB.setBounds(boundsB.x + velB.vx, boundsB.y, boundsB.width, boundsB.height);
                    if (projectedBoundsB.intersects(boundsA)) {
                        if ((isAPlayer && isBNPC) || (isANPC && isBPlayer)) log.debug("EvE Stop: B ({}) stops X-move due to A ({})", entityB, entityA);
                        velB.vx = 0; // Stop B's horizontal move
                    }
                }

                // Check A moving vertically into B's current position (using potentially modified X velocity)
                if (velAY != 0) {
                    projectedBoundsA.setBounds(posA.x + colA.offsetX + velA.vx, posA.y + colA.offsetY + velAY, colA.hitboxWidth, colA.hitboxHeight); // Use current velA.vx
                    boundsB.setBounds(posB.x + colB.offsetX, posB.y + colB.offsetY, colB.hitboxWidth, colB.hitboxHeight); // B's current bounds
                    if (projectedBoundsA.intersects(boundsB)) {
                        if ((isAPlayer && isBNPC) || (isANPC && isBPlayer)) log.debug("EvE Stop: A ({}) stops Y-move due to B ({})", entityA, entityB);
                        velA.vy = 0; // Stop A's vertical move
                        velAY = 0;   // Update local copy
                    }
                }
                // Check B moving vertically into A's current position (using potentially modified X velocity)
                if (velB.vy != 0) {
                    projectedBoundsB.setBounds(posB.x + colB.offsetX + velB.vx, posB.y + colB.offsetY + velB.vy, colB.hitboxWidth, colB.hitboxHeight); // Use current velB.vx
                    boundsA.setBounds(posA.x + colA.offsetX, posA.y + colA.offsetY, colA.hitboxWidth, colA.hitboxHeight); // A's current bounds
                    if (projectedBoundsB.intersects(boundsA)) {
                        if ((isAPlayer && isBNPC) || (isANPC && isBPlayer)) log.debug("EvE Stop: B ({}) stops Y-move due to A ({})", entityB, entityA);
                        velB.vy = 0; // Stop B's vertical move
                    }
                }
            } // End inner loop (j)
        } // End outer loop (i)
    }

    private void checkEntityVsTileCollision(List<Integer> collidableEntities, int tileSize) {
        // (Resolves collisions with static level geometry first)
        for (int entity : collidableEntities) {
            // Check components again in case removed mid-frame? Unlikely but safe.
            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            VelocityComponent vel = entityManager.getComponent(entity, VelocityComponent.class);
            ColliderComponent col = entityManager.getComponent(entity, ColliderComponent.class);
            ParticlePhysicsComponent particlePhysics = entityManager.getComponent(entity, ParticlePhysicsComponent.class);
            boolean isParticle = (particlePhysics != null);

            if (pos == null || vel == null || col == null) continue;

            // Get the intended velocity for this frame (usually set by input or AI systems)
            // Note: We directly modify the vel component based on collisions.
            int currentVx = vel.vx;
            int currentVy = vel.vy;
            // Store the final velocity after collision checks
            int finalVx = currentVx;
            int finalVy = currentVy;
            // --- Calculate current hitbox top-left position using offsets ---
            // Use the helper methods from ColliderComponent for clarity
            int hitboxX = col.getHitboxX(pos); // pos.x + col.offsetX
            int hitboxY = col.getHitboxY(pos); // pos.y + col.offsetY

            // Horizontal Tile Check
            if (currentVx != 0) {
                if (isCollidingWithTile(hitboxX, hitboxY, currentVx, 0, col.hitboxWidth, col.hitboxHeight, tileSize)) {
                    if (isParticle) {
                        /* Particle damping logic */
                        finalVx = (int)(currentVx * particlePhysics.wallDamping);
                        vel.vy = (int)(vel.vy * particlePhysics.wallDamping);
                        particlePhysics.vz *= particlePhysics.wallDamping;
                    } else {
                        /* Normal sliding logic */
                        if (currentVx > 0) {
                            int edgeX = hitboxX + col.hitboxWidth - 1;
                            int tileX = (edgeX + currentVx) / tileSize;
                            finalVx = tileX * tileSize - edgeX - 1;
                        } else {
                            int edgeX = hitboxX;
                            int tileX = (edgeX + currentVx) / tileSize;
                            finalVx = (tileX + 1) * tileSize - edgeX;
                        } if ((currentVx > 0 && finalVx < 0) || (currentVx < 0 && finalVx > 0))
                            finalVx = 0;
                    }
                }
            }
            // Vertical Tile Check (using potentially adjusted X from horizontal slide if implemented)
            // For simplicity, check vertical independently using original hitboxX and finalVx=0
             if (currentVy != 0) {
                 if (isCollidingWithTile(hitboxX, hitboxY, finalVx, currentVy, col.hitboxWidth, col.hitboxHeight, tileSize)) { // Check using finalVx=0 for vertical test
                     if (isParticle) {
                     /* Particle damping logic */
                       finalVy = (int)(currentVy * particlePhysics.wallDamping);
                       vel.vx = (int)(finalVx * particlePhysics.wallDamping);
                       particlePhysics.vz *= particlePhysics.wallDamping;
                     } // Use finalVx here
                     else {
                     /* Normal sliding logic */
                     if (currentVy > 0) {
                     int edgeY = hitboxY + col.hitboxHeight - 1;
                     int tileY = (edgeY + currentVy) / tileSize;
                      finalVy = tileY * tileSize - edgeY - 1;
                       } else {
                       int edgeY = hitboxY;
                        int tileY = (edgeY + currentVy) / tileSize;
                         finalVy = (tileY + 1) * tileSize - edgeY;
                          } if ((currentVy > 0 && finalVy < 0) || (currentVy < 0 && finalVy > 0))
                          finalVy = 0;
                           }
                 }
            }

            // Update the entity's velocity component with the final, collision-adjusted values
            vel.vx = finalVx;
            vel.vy = finalVy;
        } // End Entity-vs-Tile loop
    }

    private List<Integer> getCollidableEntities() {
        Set<Integer> entitiesWithRequired = entityManager.getEntitiesWith(PositionComponent.class, VelocityComponent.class, ColliderComponent.class);
        List<Integer> collidableEntities = new ArrayList<>();
        for (int entityId : entitiesWithRequired) {
             // Filter out dead entities
             if (!entityManager.hasComponent(entityId, DeadComponent.class)) {
                 collidableEntities.add(entityId);
             }
        }
        // log.trace("Collidable entities this frame: {}", collidableEntities); // Debug: See who is collidable
        return collidableEntities;
    }


    /** Checks for collision with solid tiles */
    private boolean isCollidingWithTile(int x, int y, int xa, int ya, int hitboxWidth, int hitboxHeight, int tileSize) {
        // Check all 4 corners of the hitbox's potential future position
        for (int c = 0; c < 4; c++) {
            // Calculate corner offsets relative to the hitbox top-left
            // Use (width - 1) and (height - 1) to get the coordinates of the far edges.
            int cornerXOffset = (c % 2) * (hitboxWidth - 1);
            int cornerYOffset = (c / 2) * (hitboxHeight - 1);

            // Calculate the absolute world coordinates of the corner *after* the potential move
            int futureCornerX = x + xa + cornerXOffset;
            int futureCornerY = y + ya + cornerYOffset;

            // Convert the world coordinates of the corner to tile coordinates
            int tileX = futureCornerX / tileSize;
            int tileY = futureCornerY / tileSize;

            // Check if the tile at these coordinates is solid using the Level object
            if (level.isSolid(tileX, tileY)) {
                // System.out.println("Collision Check: Corner " + c + " at world (" + futureCornerX + "," + futureCornerY + ") -> tile (" + tileX + "," + tileY + ") is Solid."); // Detailed debug
                return true; // Collision detected
            }
        }
        // No collision detected at any of the four corners
        return false;
    }

    // AABB check could also be a static helper method
    // private boolean checkAABBOverlap(PositionComponent posA, ColliderComponent colA, PositionComponent posB, ColliderComponent colB) {
    //     int leftA = posA.x + colA.offsetX;
    //     int rightA = leftA + colA.hitboxWidth;
    //     int topA = posA.y + colA.offsetY;
    //     int bottomA = topA + colA.hitboxHeight;
    //
    //     int leftB = posB.x + colB.offsetX;
    //     int rightB = leftB + colB.hitboxWidth;
    //     int topB = posB.y + colB.offsetY;
    //     int bottomB = topB + colB.hitboxHeight;
    //
    //     // Check for non-overlap
    //     if (rightA <= leftB || leftA >= rightB || bottomA <= topB || topA >= bottomB) {
    //         return false; // No overlap
    //     }
    //     return true; // Overlap
    // }
}
