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
            // Efficiently read all pixel data into a 1D array
            int[] pixelData = new int[width * height];
            image.getRGB(0, 0, width, height, pixelData, 0, width);

            // Iterate through the 1D pixel data array
            for (int i = 0; i < pixelData.length; i++) {
                int pixelColor = pixelData[i]; // Get the ARGB color for this pixel

                // Calculate corresponding x, y coordinates
                int x = i % width;
                int y = i / width;

                // --- Determine Tile Type based on Color ---
                // Use the static helper method from the Tile class
                Tile tile = Tile.getTileFromColor(pixelColor);
                // -----------------------------------------

                // --- Store the determined Tile object in the Level ---
                // This now calls the updated Level.setTile method
                level.setTile(x, y, tile);
                // -------------------------------------------------
            }

            System.out.println("Map loading complete.");
            // Optionally report if a spawn point was found
            if (level.getPlayerSpawnX() != -1) {
                System.out.println("Player spawn point detected during map load.");
            } else {
                System.out.println("No player spawn point color detected in map.");
            }
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
