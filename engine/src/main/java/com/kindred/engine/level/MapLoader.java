package com.kindred.engine.level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MapLoader {

    // Define colors for map elements (using full ARGB hex format)
    // Make sure these EXACTLY match the colors in your spawn.png image
    private static final int COLOR_SOLID_WALL = 0xFF000000; // Opaque Black = Solid
    private static final int COLOR_WALKABLE = 0xFFFFFFFF;   // Opaque White = Walkable (Example)
    // Add other colors as needed, e.g., COLOR_PLAYER_SPAWN = 0xFFFF0000; (Opaque Red)

    public static Level loadLevelFromImage(String path, int tileSize) {
        System.out.println("Loading map from: " + path); // Debug output
        try (InputStream stream = MapLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Cannot find map resource: " + path + ". Make sure it's in the classpath.");
            }
            BufferedImage image = ImageIO.read(stream);
            Objects.requireNonNull(image, "ImageIO.read returned null for path: " + path); // Ensure image loaded

            int width = image.getWidth();
            int height = image.getHeight();
            System.out.println("Map dimensions: " + width + "x" + height); // Debug output

            Level level = new Level(width, height, tileSize);
            boolean firstPixel = true; // Flag to print only the first few pixels

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y); // Gets ARGB color
                    boolean solid = false;
                    int tileColor = pixel; // Store the color for rendering/identification

                    // --- Debug: Print first few pixel values ---
                    if (firstPixel && x < 5 && y < 5) { // Limit debug output
                        System.out.printf("DEBUG: Pixel at (%d, %d) = 0x%08X (int: %d)%n", x, y, pixel, pixel);
                        if (x == 4 && y == 4) firstPixel = false; // Stop printing after a few
                    }
                    // -----------------------------------------

                    // --- Solidity Logic based on Color ---
                    if (pixel == COLOR_SOLID_WALL) { // Check if the pixel color is EXACTLY black
                        solid = true;
                        // Optional Debug: System.out.printf("DEBUG: Tile (%d, %d) SOLID (Is Black: 0x%08X)%n", x, y, pixel);
                    } else {
                        // Treat other defined colors or default as non-solid
                        solid = false;
                        // Optional Debug: System.out.printf("DEBUG: Tile (%d, %d) NOT SOLID (Color: 0x%08X)%n", x, y, pixel);
                    }
                    // ------------------------------------

                    // Set the tile using the determined solidity and the original pixel color
                    level.setTile(x, y, tileColor, solid);
                }
            }
            System.out.println("Map loading complete."); // Debug output
            return level;
        } catch (IOException | NullPointerException e) { // Catch potential null from ImageIO.read
            // Log the error more informatively
            System.err.println("Failed to load map image: " + path);
            e.printStackTrace();
            // Re-throw as a RuntimeException to halt execution if map loading fails critically
            throw new RuntimeException("Failed to load map: " + path, e);
        }
    }
}
