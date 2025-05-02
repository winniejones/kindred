package com.kindred.engine.resource;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AssetLoader {
    // Cache to store already loaded images (maps path to image)
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    private static final Map<String, BufferedImage> placeholderCache = new HashMap<>();

    /**
     * Loads an image from the classpath (e.g. /assets/sprites/player.png)
     */
    public static BufferedImage loadImage(String path) {
        // 1. Check cache first
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        // 2. Load from classpath resource stream
        try (InputStream is = AssetLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Asset not found: " + path);
            }
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IOException("ImageIO.read returned null for path: " + path);
            }
            log.info("AssetLoader: Loaded image {} ({}x{})", path, image.getWidth(), image.getHeight());
            // 3. Store in cache on success
            imageCache.put(path, image);
            return image;
        } catch (IOException e) {
            // 4. Handle error: print error and return placeholder
            log.error("Failed to load image: {}", path, e);
            return createPlaceholderImage(1, 1);
        }
    }

    /**
     * Extracts a square sub-sprite from a sprite sheet.
     * @param sheet The source sprite sheet (should be loaded via loadImage).
     * @param col Column index (zero-based).
     * @param row Row index (zero-based).
     * @param tileSize Size (width and height) of the sprite.
     * @return The extracted sprite as a BufferedImage, or a placeholder if sheet is null or coords are invalid.
     */
    public static BufferedImage getSprite(BufferedImage sheet, int col, int row, int tileSize) {
        return getSprite(sheet, col, row, tileSize, tileSize);
    }

    /**
     * Extracts a potentially non-square sub-sprite from a sprite sheet.
     * Includes bounds checking and returns a placeholder on failure.
     *
     * @param sheet The source sprite sheet (should be loaded via loadImage).
     * @param col The column index of the sprite (0-based).
     * @param row The row index of the sprite (0-based).
     * @param spriteWidth The width of a single sprite in pixels.
     * @param spriteHeight The height of a single sprite in pixels.
     * @return The extracted sprite as a BufferedImage, or a placeholder if sheet is null or coords are invalid.
     */
    public static BufferedImage getSprite(BufferedImage sheet, int col, int row, int spriteWidth, int spriteHeight) {
        // 1. Check if sheet is valid (and not a placeholder itself)
        if (sheet == null || sheet.getWidth() <= 1 || sheet.getHeight() <= 1) {
            // Don't log error here if sheet is placeholder, just return another placeholder
            return createPlaceholderImage(spriteWidth, spriteHeight);
        }

        // 2. Calculate pixel coordinates
        int x = col * spriteWidth;
        int y = row * spriteHeight;

        // 3. Perform bounds check BEFORE calling getSubimage
        if (x < 0 || y < 0 || x + spriteWidth > sheet.getWidth() || y + spriteHeight > sheet.getHeight()) {
            log.error("AssetLoader Error: Sprite coordinates (col:{}, row:{}) with size ({}x{}) are out of bounds for sheet size ({}x{}).",
                    col, row, spriteWidth, spriteHeight, sheet.getWidth(), sheet.getHeight());
            return createPlaceholderImage(spriteWidth, spriteHeight); // Return placeholder
        }

        // 4. Try to extract subimage, return placeholder on failure
        try {
            return sheet.getSubimage(x, y, spriteWidth, spriteHeight);
        } catch (Exception e) {
            log.error("AssetLoader Error: Failed to get subimage at pixel coords ({}, {}) for sprite (col:{}, row:{}) size ({}x{}).",
                    x, y, col, row, spriteWidth, spriteHeight, e);
            return createPlaceholderImage(spriteWidth, spriteHeight); // Return placeholder
        }
    }

    /**
     * Helper method to load a sequence of animation frames from a spritesheet row or column.
     * Assumes frames are arranged contiguously.
     *
     * @param sheetPath Path to the spritesheet image file.
     * @param startCol The starting column index of the first frame.
     * @param startRow The starting row index of the first frame.
     * @param numFrames The number of frames in the sequence.
     * @param spriteWidth Width of each frame.
     * @param spriteHeight Height of each frame.
     * @param horizontal True if frames are arranged horizontally (read across columns), false if vertically (read down rows).
     * @return A List of BufferedImage frames (may contain placeholders if individual frames fail),
     * or an empty list if the sheet itself fails to load.
     */
    public static List<BufferedImage> loadAnimationFrames(String sheetPath, int startCol, int startRow, int numFrames, int spriteWidth, int spriteHeight, boolean horizontal) {
        List<BufferedImage> frames = new ArrayList<>();
        BufferedImage sheet = loadImage(sheetPath); // Load the sheet (uses cache and error handling)

        // Check if the sheet loading failed (loadImage returns placeholder)
        if (sheet == null || sheet.getWidth() <= 1 || sheet.getHeight() <= 1) {
            log.error("AssetLoader Error: Failed to load sheet or sheet is placeholder for animation: {}", sheetPath);
            return frames; // Return empty list
        }

        for (int i = 0; i < numFrames; i++) {
            int currentCol = horizontal ? startCol + i : startCol;
            int currentRow = horizontal ? startRow : startRow + i;
            // Use the getSprite overload that takes width/height
            // getSprite returns a placeholder on failure, so we always add something
            BufferedImage frame = getSprite(sheet, currentCol, currentRow, spriteWidth, spriteHeight);
            frames.add(frame);
            // Optional: Check if the returned frame is a placeholder and log warning
            // if (frame.getWidth() <= 1 || frame.getHeight() <= 1) {
            //    log.warn("AssetLoader Warning: Loaded placeholder for animation frame {} at ({}, {}) from {}", i, currentCol, currentRow, sheetPath);
            // }
        }
        return frames;
    }

    /**
     * Clears the image cache. Useful if assets need to be reloaded during development.
     */
    public static void clearCache() {
        imageCache.clear();
        placeholderCache.clear();
        log.info("AssetLoader: Image caches cleared.");
    }

    /**
     * Creates or retrieves a cached placeholder image (magenta square).
     * Used as a return value when image loading or sprite extraction fails.
     * @param width Width of the placeholder.
     * @param height Height of the placeholder.
     * @return A cached or newly created BufferedImage placeholder.
     */
    public static BufferedImage createPlaceholderImage(int width, int height) {
        // Ensure minimum size of 1x1
        width = Math.max(1, width);
        height = Math.max(1, height);
        String key = width + "x" + height; // Cache key based on size

        // Check placeholder cache first
        if (placeholderCache.containsKey(key)) {
            return placeholderCache.get(key);
        }

        // Create placeholder if not cached
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        try { // Ensure graphics object is disposed
            g.setColor(new Color(255, 0, 255)); // Magenta
            g.fillRect(0, 0, width, height);
        } finally {
            g.dispose();
        }
        // Store in cache and return
        placeholderCache.put(key, placeholder);
        return placeholder;
    }
}
