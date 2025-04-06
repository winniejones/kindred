package com.kindred.engine.resource;

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

public class AssetLoader {
    // Cache to store already loaded images (maps path to image)
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    private static BufferedImage placeholderTile = null; // Placeholder for getSprite failures

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
                // Keep original behavior: throw exception which gets caught below
                throw new IOException("Asset not found: " + path);
            }
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                // Throw exception if ImageIO fails to read
                throw new IOException("ImageIO.read returned null for path: " + path);
            }
            System.out.println("AssetLoader: Loaded image " + path + " (" + image.getWidth() + "x" + image.getHeight() + ")");
            // 3. Store in cache on success
            imageCache.put(path, image);
            return image;
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
            // Return a minimal placeholder (consistent with original code)
            return createPlaceholderImage(1, 1);
        }
    }

    /**
     * Extracts a sub-sprite from a sprite sheet.
     * @param sheet The source sprite sheet
     * @param col Column index (zero-based)
     * @param row Row index (zero-based)
     * @param tileSize Size of each sprite (e.g. 32 for 32x32)
     */
    public static BufferedImage getSprite(BufferedImage sheet, int col, int row, int tileSize) {
        return sheet.getSubimage(col * tileSize, row * tileSize, tileSize, tileSize);
    }

    /**
     * Extracts a potentially non-square sub-sprite from a sprite sheet.
     * Includes bounds checking and returns a placeholder on failure.
     * (Overload added to support non-square sprites and animation helper)
     *
     * @param sheet The source sprite sheet (should be loaded via loadImage).
     * @param col The column index of the sprite (0-based).
     * @param row The row index of the sprite (0-based).
     * @param spriteWidth The width of a single sprite in pixels.
     * @param spriteHeight The height of a single sprite in pixels.
     * @return The extracted sprite as a BufferedImage, or a placeholder if sheet is null or coords are invalid.
     */
    public static BufferedImage getSprite(BufferedImage sheet, int col, int row, int spriteWidth, int spriteHeight) {
        // 1. Check if sheet is valid
        if (sheet == null) {
            System.err.println("AssetLoader Error: Cannot getSprite from a null spritesheet.");
            return createPlaceholderImage(spriteWidth, spriteHeight); // Return placeholder matching expected size
        }

        // 2. Calculate pixel coordinates
        int x = col * spriteWidth;
        int y = row * spriteHeight;

        // 3. Perform bounds check BEFORE calling getSubimage
        if (x < 0 || y < 0 || x + spriteWidth > sheet.getWidth() || y + spriteHeight > sheet.getHeight()) {
            System.err.printf("AssetLoader Error: Sprite coordinates (col:%d, row:%d) with size (%dx%d) are out of bounds for sheet size (%dx%d).%n",
                    col, row, spriteWidth, spriteHeight, sheet.getWidth(), sheet.getHeight());
            return createPlaceholderImage(spriteWidth, spriteHeight); // Return placeholder
        }

        // 4. Try to extract subimage, return placeholder on failure
        try {
            return sheet.getSubimage(x, y, spriteWidth, spriteHeight);
        } catch (Exception e) {
            System.err.printf("AssetLoader Error: Failed to get subimage at pixel coords (%d, %d) for sprite (col:%d, row:%d) size (%dx%d).%n",
                    x, y, col, row, spriteWidth, spriteHeight);
            e.printStackTrace();
            return createPlaceholderImage(spriteWidth, spriteHeight); // Return placeholder
        }
    }

    /**
     * Helper method to load a sequence of animation frames from a spritesheet row or column.
     * (Added from generated code)
     *
     * @param sheetPath Path to the spritesheet image file.
     * @param startCol The starting column index of the first frame.
     * @param startRow The starting row index of the first frame.
     * @param numFrames The number of frames in the sequence.
     * @param spriteWidth Width of each frame.
     * @param spriteHeight Height of each frame.
     * @param horizontal True if frames are arranged horizontally, false if vertically.
     * @return A List of BufferedImage frames, or an empty list if loading fails.
     */
    public static List<BufferedImage> loadAnimationFrames(String sheetPath, int startCol, int startRow, int numFrames, int spriteWidth, int spriteHeight, boolean horizontal) {
        List<BufferedImage> frames = new ArrayList<>();
        BufferedImage sheet = loadImage(sheetPath); // Load the sheet (uses cache and error handling)

        // loadImage returns a placeholder on failure, check dimensions
        if (sheet == null || sheet.getWidth() <= 1 || sheet.getHeight() <= 1) {
            System.err.println("AssetLoader Error: Failed to load sheet or sheet is placeholder for animation: " + sheetPath);
            return frames; // Return empty list if sheet failed to load
        }


        for (int i = 0; i < numFrames; i++) {
            int currentCol = horizontal ? startCol + i : startCol;
            int currentRow = horizontal ? startRow : startRow + i;
            // Use the getSprite overload that takes width/height
            BufferedImage frame = getSprite(sheet, currentCol, currentRow, spriteWidth, spriteHeight);
            // getSprite now returns a placeholder on failure, so we can always add it,
            // but maybe check dimensions if strictness is needed.
            // if (frame != null && frame.getWidth() > 1) { // Check if it's not the 1x1 placeholder
            frames.add(frame);
            // } else {
            //     System.err.printf("AssetLoader Warning: Failed to load animation frame %d at (%d, %d) from %s%n", i, currentCol, currentRow, sheetPath);
            // }
        }
        return frames;
    }

    /**
     * Clears the image cache. Useful if assets need to be reloaded.
     * (Added from generated code)
     */
    public static void clearCache() {
        imageCache.clear();
        System.out.println("AssetLoader: Image cache cleared.");
    }

    /**
     * Creates a simple placeholder image (e.g., magenta square).
     * Used as a return value when image loading or sprite extraction fails.
     * @param width Width of the placeholder.
     * @param height Height of the placeholder.
     * @return A BufferedImage placeholder.
     */
    private static BufferedImage createPlaceholderImage(int width, int height) {
        // Ensure minimum size of 1x1
        width = Math.max(1, width);
        height = Math.max(1, height);
        // Use a simple magenta color often used for missing textures
        // Use TYPE_INT_ARGB to ensure compatibility
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        g.setColor(new Color(255, 0, 254)); // Magenta
        g.fillRect(0, 0, width, height);
        g.dispose();
        return placeholder;

        // Cache the 1x1 placeholder specifically if needed often
        // if (width == 1 && height == 1) {
        //    if (placeholderTile == null) {
        //        placeholderTile = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        //        placeholderTile.setRGB(0, 0, 0xFFFF00FF); // Magenta
        //    }
        //    return placeholderTile;
        // } else {
        //    // Create larger placeholders dynamically if needed
        //    BufferedImage ph = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //    Graphics2D g = ph.createGraphics();
        //    g.setColor(new Color(255, 0, 255)); // Magenta
        //    g.fillRect(0, 0, width, height);
        //    g.dispose();
        //    return ph;
        // }
    }
}
