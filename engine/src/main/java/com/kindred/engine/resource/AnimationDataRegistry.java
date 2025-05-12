package com.kindred.engine.resource;

import com.kindred.engine.entity.components.AnimationComponent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class AnimationDataRegistry {
    // Key: weaponType (e.g., "GENERIC_SLASH")
    // Value: BufferedImage[direction][frameIndex]
    private Map<String, BufferedImage[][]> weaponAttackAnimations;

    // Key: weaponType_direction (e.g., "GENERIC_SLASH_0" for down)
    // Value: Hitbox data for those animations (Map<frameIndex, List<Rectangle>>)
    // Storing hitboxes per direction because rotation might change their relative positions significantly.
    private Map<String, Map<Integer, List<Rectangle>>> weaponAttackHitboxesPerDirection;

    private Map<String, Float> weaponAttackFrameDurations;
    private Map<String, Integer> weaponAttackNumFrames; // Total frames in the base animation strip

    public AnimationDataRegistry() {
        weaponAttackAnimations = new HashMap<>();
        weaponAttackHitboxesPerDirection = new HashMap<>();
        weaponAttackFrameDurations = new HashMap<>();
        weaponAttackNumFrames = new HashMap<>();
        loadAllAnimationData();
    }

    private void loadAllAnimationData() {
        // --- Load Generic Slash Attack ---
        String weaponTypeKey = "GENERIC_SLASH";
        String slashSheetPath = "/assets/sprites/attack_spritesheet.png"; // Your path
        int frameWidth = 40;
        int frameHeight = 30;
        int numFrames = 4;

        weaponAttackNumFrames.put(weaponTypeKey, numFrames);
        weaponAttackFrameDurations.put(weaponTypeKey, 0.1f); // Example: 100ms per frame

        BufferedImage originalSpriteSheet = AssetLoader.loadImage(slashSheetPath);
        if (originalSpriteSheet == null || originalSpriteSheet.getWidth() <= 1) {
            System.err.println("Failed to load attack spritesheet: " + slashSheetPath);
            return;
        }

        // Extract original frames (assuming they are for UP direction)
        BufferedImage[] originalFramesUp = new BufferedImage[numFrames];
        for (int i = 0; i < numFrames; i++) {
            originalFramesUp[i] = ImageUtils.extractFrame(originalSpriteSheet, i, frameWidth, frameHeight);
        }

        // Prepare the 4-directional array
        BufferedImage[][] allDirectionFrames = new BufferedImage[4][numFrames];
        allDirectionFrames[AnimationComponent.DOWN] = originalFramesUp; // UP = 0, LEFT = 1, RIGHT = 2, DOWN = 3 (as per your AnimationComponent constants)

        // Generate other directions by rotating
        allDirectionFrames[AnimationComponent.LEFT] = new BufferedImage[numFrames];
        allDirectionFrames[AnimationComponent.UP] = new BufferedImage[numFrames];
        allDirectionFrames[AnimationComponent.RIGHT] = new BufferedImage[numFrames];

        for (int i = 0; i < numFrames; i++) {
            if (originalFramesUp[i] != null && originalFramesUp[i].getWidth() > 1) {
                // Assuming original is UP:
                // UP to RIGHT: Rotate 90 degrees clockwise
                allDirectionFrames[AnimationComponent.LEFT][i] = ImageUtils.rotateImage(originalFramesUp[i], 90);
                // UP to DOWN: Rotate 180 degrees
                allDirectionFrames[AnimationComponent.UP][i] = ImageUtils.rotateImage(originalFramesUp[i], 180);
                // UP to LEFT: Rotate 270 degrees clockwise (or -90)
                allDirectionFrames[AnimationComponent.RIGHT][i] = ImageUtils.rotateImage(originalFramesUp[i], 270);
            } else {
                // Handle placeholder if original frame failed to load
                allDirectionFrames[AnimationComponent.LEFT][i] = AssetLoader.createPlaceholderImage(frameHeight, frameWidth); // Note: dims swap
                allDirectionFrames[AnimationComponent.UP][i] = AssetLoader.createPlaceholderImage(frameWidth, frameHeight);
                allDirectionFrames[AnimationComponent.RIGHT][i] = AssetLoader.createPlaceholderImage(frameHeight, frameWidth); // Note: dims swap
            }
        }
        weaponAttackAnimations.put(weaponTypeKey, allDirectionFrames);
        System.out.println("Loaded and processed GENERIC_SLASH animations.");

        // --- Define Hitboxes (CRITICAL: These need to be adjusted per direction after rotation) ---
        // This part is complex because hitboxes also need to be "rotated" or defined per direction.
        // For simplicity, let's define a sample hitbox for the UP direction and acknowledge
        // that for other directions, these relative coordinates would need transformation.

        // Example Hitboxes for UP direction (relative to entity origin, assuming entity faces UP)
        // You'll need to carefully define these based on your visuals.
        // Map<frameIndex, List<Rectangle>>
        Map<Integer, List<Rectangle>> hitboxesUp = new HashMap<>();
        // Frame 0: No hitbox (wind-up)
        // Frame 1: Active
        List<Rectangle> frame1HitboxesUp = new ArrayList<>();
        frame1HitboxesUp.add(new Rectangle(-15, -25, 30, 20)); // x, y, width, height (example for an upward slash)
        hitboxesUp.put(1, frame1HitboxesUp);
        // Frame 2: Active
        List<Rectangle> frame2HitboxesUp = new ArrayList<>();
        frame2HitboxesUp.add(new Rectangle(-20, -30, 40, 25)); // Wider/longer
        hitboxesUp.put(2, frame2HitboxesUp);
        // Frame 3: Maybe a lingering small hitbox or none
        weaponAttackHitboxesPerDirection.put(weaponTypeKey + "_" + AnimationComponent.UP, hitboxesUp);


        // TODO: Define/Transform hitboxes for RIGHT, DOWN, LEFT directions.
        // This is non-trivial. Rotating a rectangle requires finding the new bounding box
        // or transforming its corner points. A simpler approach for gameplay might be to
        // define distinct (even if symmetrical) hitboxes for each cardinal direction's animation.
        // For example, if the UP slash is (-15, -25, 30, 20), a RIGHT slash might be (5, -15, 20, 30) relative to origin.
        // This needs careful design based on your sprite's pivot and visual extent.

        // Placeholder for other directions' hitboxes (you'll need to create these)
        weaponAttackHitboxesPerDirection.put(weaponTypeKey + "_" + AnimationComponent.LEFT, createTransformedHitboxes(hitboxesUp, 90, frameWidth, frameHeight, frameHeight, frameWidth)); // Example call
        weaponAttackHitboxesPerDirection.put(weaponTypeKey + "_" + AnimationComponent.UP, createTransformedHitboxes(hitboxesUp, 180, frameWidth, frameHeight, frameWidth, frameHeight));
        weaponAttackHitboxesPerDirection.put(weaponTypeKey + "_" + AnimationComponent.RIGHT, createTransformedHitboxes(hitboxesUp, 270, frameWidth, frameHeight, frameHeight, frameWidth));

        System.out.println("Defined hitboxes for GENERIC_SLASH UP and placeholders for others.");
    }

    /**
     * Placeholder for a complex function that would transform hitbox rectangles
     * based on the rotation angle of the sprite. This is a challenging geometric problem.
     * A simpler approach is to manually define hitboxes for each direction.
     *
     * @param baseHitboxes Hitboxes for the original orientation (e.g., UP).
     * @param angle        Rotation angle in degrees.
     * @param originalSpriteW Width of the original sprite frame.
     * @param originalSpriteH Height of the original sprite frame.
     * @param rotatedSpriteW Width of the rotated sprite frame.
     * @param rotatedSpriteH Height of the rotated sprite frame.
     * @return Transformed hitboxes.
     */
    private Map<Integer, List<Rectangle>> createTransformedHitboxes(
            Map<Integer, List<Rectangle>> baseHitboxes, double angle,
            int originalSpriteW, int originalSpriteH,
            int rotatedSpriteW, int rotatedSpriteH) {

        Map<Integer, List<Rectangle>> transformedHitboxes = new HashMap<>();
        if (baseHitboxes == null) return transformedHitboxes;

        // --- THIS IS A SIMPLIFIED AND LIKELY INCORRECT TRANSFORMATION ---
        // --- Actual geometric transformation of rectangles is more involved ---
        // --- You would typically rotate the defining points of the rectangle ---
        // --- and then find the new axis-aligned bounding box (AABB).     ---
        // --- For pixel-perfect hitboxes, often defined manually per direction. ---

        double radians = Math.toRadians(angle);

        for (Map.Entry<Integer, List<Rectangle>> entry : baseHitboxes.entrySet()) {
            int frameIndex = entry.getKey();
            List<Rectangle> newRectsForFrame = new ArrayList<>();
            for (Rectangle originalRect : entry.getValue()) {
                // Center of the original hitbox relative to original sprite's top-left
                double ox = originalRect.x + originalRect.width / 2.0;
                double oy = originalRect.y + originalRect.height / 2.0;

                // Pretend sprite pivot is its center for hitbox calculation
                double spriteCenterX = originalSpriteW / 2.0;
                double spriteCenterY = originalSpriteH / 2.0;

                // Coords of hitbox center relative to sprite center
                double relX = ox - spriteCenterX;
                double relY = oy - spriteCenterY;

                // Rotate these relative coordinates
                double rotatedRelX = relX * Math.cos(radians) - relY * Math.sin(radians);
                double rotatedRelY = relX * Math.sin(radians) + relY * Math.cos(radians);

                // New sprite center in rotated sprite dimensions
                double newSpriteCenterX = rotatedSpriteW / 2.0;
                double newSpriteCenterY = rotatedSpriteH / 2.0;

                // New absolute hitbox center in rotated sprite's coordinate system
                double newAbsX = newSpriteCenterX + rotatedRelX;
                double newAbsY = newSpriteCenterY + rotatedRelY;

                // For AABB, if width/height swap, you might swap them here too
                int newRectW = (angle == 90 || angle == 270) ? originalRect.height : originalRect.width;
                int newRectH = (angle == 90 || angle == 270) ? originalRect.width : originalRect.height;

                newRectsForFrame.add(new Rectangle(
                        (int) (newAbsX - newRectW / 2.0),
                        (int) (newAbsY - newRectH / 2.0),
                        newRectW,
                        newRectH
                ));
            }
            transformedHitboxes.put(frameIndex, newRectsForFrame);
        }
        // This is a very rough approximation. For accurate hitboxes,
        // you'd typically define them manually for each rotated animation set or use
        // more precise geometric transformations (e.g., rotating polygon vertices).
        // For many games, defining them manually per direction offers the most control.
        System.out.println("WARNING: Hitbox transformation is a placeholder and likely needs manual adjustment for angle: " + angle);
        return transformedHitboxes;
    }


    public BufferedImage[][] getAttackAnimationFrames(String weaponType, int direction) {
        BufferedImage[][] allAnims = weaponAttackAnimations.get(weaponType);
        if (allAnims != null && direction >= 0 && direction < allAnims.length) {
            // Check if this specific direction has been populated
            if (allAnims[direction] != null && allAnims[direction].length > 0 &&
                    allAnims[direction][0] != null && allAnims[direction][0].getWidth() > 1) {
                return allAnims; // Return the whole 2D array, AnimationComponent will pick the direction
            } else {
                System.err.println("Animation frames for weapon " + weaponType + ", direction " + direction + " are missing or placeholders.");
                // Fallback or error handling
            }
        }
        System.err.println("Could not find attack animation frames for weaponType: " + weaponType);
        // Return a default/placeholder animation if available, or null
        return null; // Or a default placeholder animation set
    }

    public Map<Integer, List<Rectangle>> getAttackHitboxes(String weaponType, int direction) {
        String key = weaponType + "_" + direction;
        Map<Integer, List<Rectangle>> hitboxes = weaponAttackHitboxesPerDirection.get(key);
        if (hitboxes == null) {
            System.err.println("Could not find attack hitboxes for weaponType: " + weaponType + " and direction: " + direction + " (key: " + key + ")");
            return new HashMap<>(); // Return empty map to avoid null pointers
        }
        return hitboxes;
    }

    public float getAttackFrameDuration(String weaponType) {
        return weaponAttackFrameDurations.getOrDefault(weaponType, 0.1f); // Default duration
    }

    public int getNumberOfAttackFrames(String weaponType) {
        return weaponAttackNumFrames.getOrDefault(weaponType, 1); // Default to 1 frame
    }
}
