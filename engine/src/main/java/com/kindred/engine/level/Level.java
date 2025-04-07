package com.kindred.engine.level;

import com.kindred.engine.render.Screen;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Level {

    private final int width;          // Width in tiles
    private final int height;         // Height in tiles
    private final int tileSize;       // Size of each tile in pixels
    private final Tile[][] levelTiles; // 2D array storing Tile objects

    // List to store spawn points detected during map loading
    private final List<SpawnPoint> spawnPoints;

    /** Constructor */
    public Level(int width, int height, int tileSize) {
        if (width <= 0 || height <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("Level dimensions and tileSize must be positive.");
        }
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.levelTiles = new Tile[height][width];
        this.spawnPoints = new ArrayList<>(); // Initialize the list

        // Fill with VOID initially
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 levelTiles[y][x] = Tile.VOID;
             }
         }
    }

    /** Sets the Tile object at the specified tile coordinates. */
    public void setTile(int x, int y, Tile tile) {
        if (x < 0 || y < 0 || x >= width || y >= height || tile == null) {
             return; // Ignore invalid coordinates or null tile
        }
        levelTiles[y][x] = tile;
        // Spawn point detection is now handled in MapLoader *before* this is called
    }

    /** Adds a detected spawn point to the list. Called by MapLoader. */
    public void addSpawnPoint(SpawnPoint spawnPoint) {
        if (spawnPoint != null) {
            this.spawnPoints.add(spawnPoint);
            System.out.println("Added spawn point: " + spawnPoint); // Debug
        }
    }

    /** Returns an unmodifiable list of spawn points. */
    public List<SpawnPoint> getSpawnPoints() {
        // Return an unmodifiable view to prevent external modification
        return Collections.unmodifiableList(spawnPoints);
    }


    /** Renders the visible portion of the level. */
    public void render(Screen screen) {
        int x0 = screen.xOffset / tileSize - 1;
        int x1 = (screen.xOffset + screen.width + tileSize) / tileSize + 1;
        int y0 = screen.yOffset / tileSize - 1;
        int y1 = (screen.yOffset + screen.height + tileSize) / tileSize + 1;

        x0 = Math.max(0, x0);
        x1 = Math.min(width, x1);
        y0 = Math.max(0, y0);
        y1 = Math.min(height, y1);

        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                Tile tile = getTile(x, y);
                tile.render(x, y, screen, tileSize);
            }
        }
    }

    /** Gets the Tile object at the specified tile coordinates. */
    public Tile getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return Tile.VOID;
        }
        Tile tile = levelTiles[y][x];
        return (tile != null) ? tile : Tile.VOID;
    }

    /** Checks if the tile at the specified coordinates is solid. */
    public boolean isSolid(int x, int y) {
        return getTile(x, y).isSolid();
    }

    // --- Getters ---
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTileSize() { return tileSize; }

    // Remove old player spawn X/Y getters/fields
    // public int getPlayerSpawnX() { ... }
    // public int getPlayerSpawnY() { ... }
    // private int playerSpawnX = -1;
    // private int playerSpawnY = -1;
}
