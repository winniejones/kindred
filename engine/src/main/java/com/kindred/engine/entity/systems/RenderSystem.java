package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.render.Screen;

// Import SLF4J/Lombok if using logging
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

@Slf4j
public class RenderSystem implements System {

    private final EntityManager entityManager;
    private final Screen screen;

    // List to hold entities for sorting - reused to avoid reallocation
    private final List<Integer> entitiesToRender = new ArrayList<>();

    // --- Configuration for Nameplates/Health Bars ---
    private final int healthBarWidth = 16;
    private final int healthBarHeight = 2;
    private final int nameYOffset = -8; // Pixels above entity origin for name
    private final int healthBarYOffset = -3; // Pixels above entity origin for health bar
    private final Font nameFont = new Font("Arial", Font.BOLD, 8);
    private final Color healthBarBgColor = Color.DARK_GRAY;
    private final Color healthBarFgColor = Color.GREEN;
    private final Color healthBarLowColor = Color.RED; // Color when health is low
    private final float healthLowThreshold = 0.3f; // Percentage below which health bar turns red
    // ------------------------------------------------

    // --- Custom Comparator for Rendering Order ---
    private final Comparator<Integer> renderOrderComparator;

    public RenderSystem(EntityManager entityManager, Screen screen) {
        // 1. Assign final fields FIRST
        this.entityManager = entityManager;
        this.screen = screen;
        // 2. Initialize the comparator AFTER entityManager is assigned
        this.renderOrderComparator = getRenderOrderComparator(entityManager);
        log.info("RenderSystem initialized.");
    }

    @Override
    public void update(float deltaTime) {
        render();
    }

    public void render() {
        // --- Render Standard Sprites ---
        renderSprites();

        // --- Render Particles ---
        renderPractice();
        // --- End Render Particles ---

    } // End render()

    private void renderPractice() {
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, ParticleComponent.class)) {
            // Dead particles are removed by LifetimeSystem, no need to check DeadComponent
            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            ParticleComponent particle = entityManager.getComponent(entity, ParticleComponent.class);
            ParticlePhysicsComponent physics = entityManager.getComponent(entity, ParticlePhysicsComponent.class); // Get physics component

            if (pos == null || particle == null || physics == null) continue;

            // Calculate screen Y position by subtracting Z height
            int screenY = pos.y - (int) physics.z; // Adjust offset as needed
            int screenX = pos.x - particle.size / 2; // Center particle

            // Draw particle as a simple colored rectangle using fillRect
            screen.fillRect(screenX, screenY, particle.size, particle.size, particle.color, true); // true = use offset
        }
    }

    private void renderSprites() {
        // --- 1. Gather Renderable Entities ---
        prepareEntityList();

        // --- 2. Sort Entities using Custom Comparator ---
        entitiesToRender.sort(renderOrderComparator);

        // --- 3. Render Sorted Entities (Sprites + Nameplates/Health Bars) ---
        for (int entity : entitiesToRender) {
            if (!entityManager.isEntityActive(entity)) continue;

            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            SpriteComponent spriteComp = entityManager.getComponent(entity, SpriteComponent.class);
            if (pos == null || spriteComp == null || spriteComp.sprite == null) continue;

            BufferedImage currentSprite = spriteComp.sprite;
            boolean isDead = entityManager.hasComponent(entity, DeadComponent.class);
            boolean applyFlash = false;

            // Hit Flash Check (Only apply if NOT dead)
            if (!isDead && entityManager.hasComponent(entity, TookDamageComponent.class)) {
                TookDamageComponent flash = entityManager.getComponent(entity, TookDamageComponent.class);
                int flickerSegments = (int) (flash.effectTimer / 0.05f);
                if (flickerSegments % 2 == 0) {
                    applyFlash = true;
                }
            }

            if (!applyFlash) {
                // Draw standard sprite at its base X, Y position
                screen.drawSpriteWithAlpha(pos.x, pos.y, currentSprite);
            }

            // <<< Render Nameplate and Health Bar for Living Entities >>>
            if (!isDead) {
                HealthComponent health = entityManager.getComponent(entity, HealthComponent.class);
                boolean isPlayer = entityManager.hasComponent(entity, PlayerComponent.class);
                boolean isEnemy = entityManager.hasComponent(entity, EnemyComponent.class);
                NameComponent nameComp = entityManager.getComponent(entity, NameComponent.class);
                // boolean isNPC = entityManager.hasComponent(entity, NPCComponent.class); // If NPCs need names/bars

                // --- Calculate Position ---
                // Center above the entity's origin (pos.x)
                // You might want to center based on sprite width: centerX = pos.x + spriteComp.sprite.getWidth() / 2;
                int centerX = pos.x; // Simple centering on origin for now
                int nameScreenX = centerX - screen.xOffset; // Apply camera offset
                int nameScreenY = pos.y + nameYOffset - screen.yOffset;
                String entityName = (nameComp != null) ? nameComp.name : "Entity " + entity;
                int nameLength = entityName.length() * 2;
                drawEntityName(nameScreenX + nameLength, nameScreenY, entityName, health);

                // Only draw for Players and Enemies (adjust as needed)
                if ((isPlayer || isEnemy) && health != null) {
                    int barScreenX = centerX - healthBarWidth / 2 - screen.xOffset; // Center bar horizontally
                    int barScreenY = pos.y + healthBarYOffset - screen.yOffset;
                    // --- Draw Name ---

                    // --- Draw Health Bar ---
                    drawHealthBar(barScreenX + healthBarWidth, barScreenY, healthBarWidth, healthBarHeight, health.getHealthPercentage());
                }
            }
            // <<< End Nameplate / Health Bar Rendering >>>

        } // End sorted entity loop

        // --- Render Particles (Separately - Drawn On Top) ---
        for (int entity : entityManager.getEntitiesWith(PositionComponent.class, ParticleComponent.class)) {
            PositionComponent pos = entityManager.getComponent(entity, PositionComponent.class);
            ParticleComponent particle = entityManager.getComponent(entity, ParticleComponent.class);
            ParticlePhysicsComponent physics = entityManager.getComponent(entity, ParticlePhysicsComponent.class);
            if (pos == null || particle == null || physics == null) continue;
            int screenY = pos.y - (int) physics.z;
            int screenX = pos.x - particle.size / 2;
            screen.fillRect(screenX, screenY, particle.size, particle.size, particle.color, true);
        }
        // --- End Render Particles ---

    } // End render()
    private Comparator<Integer> getRenderOrderComparator(EntityManager entityManager) {
        return (entityA, entityB) -> {
            // Get components safely, handle potential nulls if entities were removed between cycles
            PositionComponent posA = entityManager.getComponent(entityA, PositionComponent.class);
            PositionComponent posB = entityManager.getComponent(entityB, PositionComponent.class);
            boolean isADead = entityManager.hasComponent(entityA, DeadComponent.class);
            boolean isBDead = entityManager.hasComponent(entityB, DeadComponent.class);

            // --- Layering Logic ---
            // 1. If one is dead and the other isn't, dead comes first (-1)
            if (isADead && !isBDead) {
                return -1; // A (dead) comes before B (alive)
            }
            if (!isADead && isBDead) {
                return 1;  // A (alive) comes after B (dead)
            }

            // --- Y-Sorting within Layers ---
            // 2. If both are dead or both are alive, sort by Y (bottom edge)
            // Handle cases where position might be null (shouldn't happen for renderables, but safe)
            int yA = (posA != null) ? getYBottom(posA, entityA) : Integer.MAX_VALUE;
            int yB = (posB != null) ? getYBottom(posB, entityB) : Integer.MAX_VALUE;

            return Integer.compare(yA, yB);
        };
    }

    // Helper to get the bottom Y coordinate for sorting
    private int getYBottom(PositionComponent pos, int entityId) {
        int height = 32; // Default height
        // Try getting height from sprite first
        SpriteComponent sc = entityManager.getComponent(entityId, SpriteComponent.class);
        if (sc != null && sc.sprite != null) {
            height = sc.sprite.getHeight();
        } else {
            // Fallback to collider height if no sprite?
            ColliderComponent col = entityManager.getComponent(entityId, ColliderComponent.class);
            if (col != null) {
                // Use collider bottom edge relative to position origin
                return pos.y + col.offsetY + col.hitboxHeight;
            }
        }
        // Default sort position if using only pos.y + sprite height
        return pos.y + height;
    }
    // --- End Custom Comparator ---

    private void prepareEntityList() {
        entitiesToRender.clear();
        Set<Integer> initialRenderableSet = entityManager.getEntitiesWith(PositionComponent.class, SpriteComponent.class);
        Set<Integer> renderableSet = new HashSet<>(initialRenderableSet);
        renderableSet.removeIf(entityId -> entityManager.hasComponent(entityId, ParticleComponent.class));
        entitiesToRender.addAll(renderableSet);
    }
    // --- Helper Drawing Methods ---

    /** Draws text centered horizontally at the given coordinates. */
    private void drawTextCentered(int screenX, int screenY, String text, Font font, Color color) {
        if (text == null || text.isEmpty()) return;
        // Need Graphics context to get FontMetrics
        // This approach requires RenderSystem to have access to Graphics or pass it down
        // Alternative: Pre-calculate text width (less accurate without Graphics)
        // For now, let's assume Screen provides a way to draw text directly
        // screen.drawTextCentered(screenX, screenY, text, font, color);

        // --- If drawing directly here (Requires Graphics g passed to render()) ---
        /*
        Graphics g = screen.getGraphics(); // Hypothetical method to get Graphics
        if (g == null) return; // Cannot draw without graphics context
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();

        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int drawX = screenX - textWidth / 2; // Adjust X to center
        int drawY = screenY + fm.getAscent() / 2; // Adjust Y slightly for vertical center?

        g.drawString(text, drawX, drawY);

        // Restore original font and color
        g.setFont(originalFont);
        g.setColor(originalColor);
        */
        // --- End Direct Drawing Example ---

        // Using Screen's fillRect for now as a placeholder if no text method exists
        // screen.fillRect(screenX - 10, screenY - 5, 20, 10, color.getRGB(), false); // fixed = false for screen coords
        log.warn("drawTextCentered needs implementation using Screen or Graphics context.");

    }

    private void drawEntityName(int nameScreenX, int nameScreenY, String name, HealthComponent health) {
        Color fgColor = health != null ? (health.getHealthPercentage() <= healthLowThreshold) ? healthBarLowColor : healthBarFgColor: Color.GREEN;
        screen.drawText(nameScreenX, nameScreenY, name, nameFont, fgColor, true);
    }

    /** Draws a health bar at the given screen coordinates. */
    private void drawHealthBar(int screenX, int screenY, int width, int height, float percentage) {
        percentage = Math.max(0f, Math.min(1f, percentage)); // Clamp 0-1

        // Draw background
        screen.fillRect(screenX, screenY, width, height, healthBarBgColor.getRGB(), false); // fixed=false for screen coords

        // Calculate foreground width and color
        int fgWidth = (int) (width * percentage);
        Color fgColor = (percentage <= healthLowThreshold) ? healthBarLowColor : healthBarFgColor;

        // Draw foreground
        if (fgWidth > 0) {
            screen.fillRect(screenX, screenY, fgWidth, height, fgColor.getRGB(), false);
        }
    }
}
