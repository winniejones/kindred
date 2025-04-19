package com.kindred.engine.level;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Slf4j
public class MapLoader {

    public static Level loadLevelFromImage(String path, int tileSize) {
        log.info("Loading map from: " + path); // Debug output
        try (InputStream stream = MapLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                 throw new IOException("Cannot find map resource: " + path);
            }
            BufferedImage image = ImageIO.read(stream);
            Objects.requireNonNull(image, "ImageIO.read returned null for path: " + path);

            int width = image.getWidth();
            int height = image.getHeight();
            log.info("Map dimensions: " + width + "x" + height); // Debug output

            Level level = new Level(width, height, tileSize);

            int[] pixelData = new int[width * height];
            image.getRGB(0, 0, width, height, pixelData, 0, width);

            for (int i = 0; i < pixelData.length; i++) {
                int pixelColor = pixelData[i];
                int x = i % width;
                int y = i / width;

                Tile tileToSet = null; // Tile to actually place in the level grid
                boolean handled = false; // Flag if pixel was a marker

                // --- Check for Spawn Marker Colors FIRST ---
                if (pixelColor == Tile.COLOR_PLAYER_SPAWN) {
                    level.addSpawnPoint(new SpawnPoint(x, y, SpawnPoint.SpawnType.PLAYER));
                    tileToSet = Tile.FLOOR; // Place floor tile underneath the marker
                    handled = true;
                } else if (pixelColor == Tile.COLOR_NPC_SPAWN) {
                    level.addSpawnPoint(new SpawnPoint(x, y, SpawnPoint.SpawnType.NPC_VILLAGER)); // Example type
                    tileToSet = Tile.FLOOR; // Place floor tile
                    handled = true;
                } else if (pixelColor == Tile.COLOR_ENEMY_SPAWN) {
                    level.addSpawnPoint(new SpawnPoint(x, y, SpawnPoint.SpawnType.ENEMY_DEIDARA)); // Example type
                    tileToSet = Tile.FLOOR; // Place floor tile
                    handled = true;
                }
                // --- Add checks for other marker colors here ---

                // --- If not a marker, determine terrain tile ---
                if (!handled) {
                    tileToSet = Tile.getTileFromColor(pixelColor);
                }
                // ---------------------------------------------

                // --- Store the determined Tile object in the Level ---
                if (tileToSet != null) { // Should not be null if logic is correct
                     level.setTile(x, y, tileToSet);
                } else {
                     // Fallback if something went wrong (shouldn't happen)
                     level.setTile(x, y, Tile.VOID);
                     System.err.printf("Error: No tile determined for pixel 0x%08X at (%d, %d)%n", pixelColor, x, y);
                }
                // -------------------------------------------------
            }

            log.info("Map loading complete. Found " + level.getSpawnPoints().size() + " spawn points.");
            return level;
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load map image: " + path);
            e.printStackTrace();
            throw new RuntimeException("Failed to load map: " + path, e);
        }
    }
}
