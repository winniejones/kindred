package com.kindred.engine.entity.systems;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.core.System;
import com.kindred.engine.render.Screen;

// Import SLF4J/Lombok if using logging
import com.kindred.engine.ui.Const;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

@Slf4j
public class RenderSystem implements System {

    private final EntityManager entityManager;
    private final Screen screen;
    private final List<Integer> entitiesToRender = new ArrayList<>();
    private final Comparator<Integer> renderOrderComparator;

    // --- Configuration for Nameplates/Health Bars ---
    private final int healthBarWidth = 16;
    private final int healthBarHeight = 2;
    private final int nameYOffset = -8; // Pixels above entity origin for name
    private final int healthBarYOffset = -3; // Pixels above entity origin for health bar
    private final Font nameFont = new Font("Arial", Font.BOLD, 8);
    private final Color healthBarBgColor = Color.DARK_GRAY;
    private final Color healthBarFgColor = Color.GREEN;
    private final Color healthBarLowColor = Color.RED;
    private final float healthLowThreshold = 0.3f;


    public RenderSystem(EntityManager entityManager, Screen screen) {
        // 1. Assign final fields FIRST
        this.entityManager = entityManager;
        this.screen = screen;
        // 2. Initialize the comparator AFTER entityManager is assigned
        this.renderOrderComparator = getRenderOrderComparator(entityManager);
        log.info("RenderSystem initialized.");
    }

    private int getYBottom(PositionComponent pos, int entityId) {
        SpriteComponent sc = entityManager.getComponent(entityId, SpriteComponent.class);
        if (sc != null && sc.sprite != null) {
            return pos.y + sc.sprite.getHeight();
        }
        // Fallback if no sprite, or use collider bottom
        ColliderComponent col = entityManager.getComponent(entityId, ColliderComponent.class);
        if (col != null) {
            return pos.y + col.offsetY + col.hitboxHeight;
        }
        return pos.y + 32; // Assuming a default height for sorting if no sprite info
    }


    @Override
    public void update(float deltaTime) {
        // RenderSystem's main job is in render(), not typically update(deltaTime)
        // unless it has its own animations or effects to manage over time.
    }

    public void render() {
        entitiesToRender.clear();
        // Get entities with base sprite and position for initial render list
        Set<Integer> baseRenderables = entityManager.getEntitiesWith(PositionComponent.class, SpriteComponent.class);
        entitiesToRender.addAll(baseRenderables);

        // Sort entities by Y position for painter's algorithm
        // (Dead entities are already handled by the comparator to be drawn first within their Y-sort)
        entitiesToRender.sort(renderOrderComparator);

        for (int entityId : entitiesToRender) {
            if (!entityManager.isEntityActive(entityId)) continue;

            PositionComponent pos = entityManager.getComponent(entityId, PositionComponent.class);
            SpriteComponent spriteComp = entityManager.getComponent(entityId, SpriteComponent.class);

            if (pos == null || spriteComp == null || spriteComp.sprite == null) continue;

            // 1. Draw the entity's base sprite (character, corpse, etc.)
            // The sprite in spriteComp is managed by AnimationSystem (for walk/idle)
            // or CorpseDecaySystem (for dead entities).
            screen.drawSpriteWithOffset(pos.x, pos.y, spriteComp.sprite); // Assuming drawSpriteWithOffset handles camera

            // +++ NEW: Render Attack Visual Effect on top +++
            if (entityManager.hasComponent(entityId, AttackVisualEffectComponent.class)) {
                AttackVisualEffectComponent effectComp = entityManager.getComponent(entityId, AttackVisualEffectComponent.class);
                if (effectComp != null) {
                    BufferedImage effectFrame = effectComp.getCurrentVisualFrame();
                    if (effectFrame != null) {
                        int attackerBaseX = pos.x;
                        int attackerBaseY = pos.y;

                        int attackerWidth = 32;
                        int attackerHeight = 32;
                        if (spriteComp.sprite != null) { // Use actual sprite dimensions if available
                            attackerWidth = spriteComp.sprite.getWidth();
                            attackerHeight = spriteComp.sprite.getHeight();
                        }

                        int effectWidth = effectFrame.getWidth();
                        int effectHeight = effectFrame.getHeight();

                        // Desired offset factor (e.g., 0.35 means 35% of attacker's dimension "in front")
                        // This factor determines how much "in front" the effect's *origin* is placed.
                        // A smaller value (e.g., 0.1f to 0.25f) might be better if the effect sprite itself has empty space.
                        float offsetFactor = 0.05f; // Tunable: 25% to 50% (0.25f to 0.5f)

                        int finalEffectX = attackerBaseX;
                        int finalEffectY = attackerBaseY;

                        switch (effectComp.direction) {
                            case AnimationComponent.UP:
                                finalEffectX = attackerBaseX + (attackerWidth / 2) - (effectWidth / 2);
                                // Position effect's origin (top-left) such that it appears in front.
                                // If effect sprite's visual starts at its top, this moves it up.
                                finalEffectY = attackerBaseY - (int)(attackerHeight * offsetFactor) - effectHeight / 2 ; // Adjusted to better center and push
                                break;
                            case AnimationComponent.DOWN:
                                finalEffectX = attackerBaseX + (attackerWidth / 2) - (effectWidth / 2);
                                // Position effect's origin below the attacker
                                finalEffectY = attackerBaseY + (int)(attackerHeight * offsetFactor) + attackerHeight / 2; // Adjusted
                                break;
                            case AnimationComponent.LEFT:
                                finalEffectY = attackerBaseY + (attackerHeight / 2) - (effectHeight / 2);
                                // Position effect's origin to the left of the attacker
                                finalEffectX = attackerBaseX - (int)(attackerWidth * offsetFactor) - effectWidth / 2; // Adjusted
                                break;
                            case AnimationComponent.RIGHT:
                                finalEffectY = attackerBaseY + (attackerHeight / 2) - (effectHeight / 2);
                                // Position effect's origin to the right of the attacker
                                finalEffectX = attackerBaseX + (int)(attackerWidth * offsetFactor) + attackerWidth / 2; // Adjusted
                                break;
                        }
                        screen.drawSpriteWithOffset(finalEffectX, finalEffectY, effectFrame);
                    }
                }
            }

            // Render Nameplate and Health Bar (your existing logic)
            if (!entityManager.hasComponent(entityId, DeadComponent.class)) {
                HealthComponent health = entityManager.getComponent(entityId, HealthComponent.class);
                NameComponent nameComp = entityManager.getComponent(entityId, NameComponent.class);

                int centerX = pos.x + ((spriteComp.sprite != null) ? spriteComp.sprite.getWidth() / 2 : 16); // Center based on sprite

                if (nameComp != null) {
                    int nameScreenX = centerX - screen.xOffset;
                    int nameScreenY = pos.y + nameYOffset - screen.yOffset;
                    Color nameColor = Color.WHITE; // Default
                    if(entityManager.hasComponent(entityId, PlayerComponent.class)) nameColor = Const.COLOR_TEXT_PLAYER_NAME;
                    else if(entityManager.hasComponent(entityId, EnemyComponent.class)) nameColor = Const.COLOR_TEXT_ENEMY_NAME;

                    screen.drawText(nameScreenX, nameScreenY, nameComp.name, nameFont, nameColor, true);
                }

                if (health != null && (entityManager.hasComponent(entityId, PlayerComponent.class) || entityManager.hasComponent(entityId, EnemyComponent.class))) {
                    int barScreenX = centerX - healthBarWidth / 2 - screen.xOffset;
                    int barScreenY = pos.y + healthBarYOffset - screen.yOffset;
                    drawHealthBar(barScreenX, barScreenY, healthBarWidth, healthBarHeight, health.getHealthPercentage());
                }
            }
        } // End entity loop

        // Render Particles (if they are separate and drawn last)
        // Your existing particle rendering logic can go here if it's not part of the main entity loop.
        // For example:
        for (int particleEntity : entityManager.getEntitiesWith(PositionComponent.class, ParticleComponent.class)) {
            PositionComponent particlePos = entityManager.getComponent(particleEntity, PositionComponent.class);
            ParticleComponent particle = entityManager.getComponent(particleEntity, ParticleComponent.class);
            ParticlePhysicsComponent physics = entityManager.getComponent(particleEntity, ParticlePhysicsComponent.class);

            if (particlePos == null || particle == null || physics == null) continue;

            int screenY = particlePos.y - (int) physics.z;
            int screenX = particlePos.x - particle.size / 2;
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
