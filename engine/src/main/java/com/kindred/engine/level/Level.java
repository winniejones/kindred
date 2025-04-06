package com.kindred.engine.level;

import com.kindred.engine.render.Screen;

public class Level {

    private final int width;          // Width in tiles
    private final int height;         // Height in tiles
    private final int tileSize;       // Size of each tile in pixels
    private final Tile[][] levelTiles; // 2D array storing Tile objects

    private int playerSpawnX = -1;
    private int playerSpawnY = -1;

    /**
     * Constructor for Level using Tile objects.
     * @param width Width of the level in tiles.
     * @param height Height of the level in tiles.
     * @param tileSize Size of one tile in pixels.
     */
    public Level(int width, int height, int tileSize) {
        if (width <= 0 || height <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("Level dimensions and tileSize must be positive.");
        }
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.levelTiles = new Tile[height][width]; // Initialize the 2D Tile array

        // Fill with VOID initially to avoid null pointers before MapLoader runs
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                levelTiles[y][x] = Tile.VOID; // Default to void
            }
        }
    }

    /**
     * Renders the visible portion of the level based on the Screen's offset.
     * @param screen The Screen object to render onto, containing offset info.
     */
    public void render(Screen screen) {
        // Calculate the tile range to render based on screen dimensions and offset
        // Add/subtract 1 tile buffer to prevent visual gaps at edges when moving
        int x0 = screen.xOffset / tileSize - 1;
        int x1 = (screen.xOffset + screen.width + tileSize) / tileSize + 1;
        int y0 = screen.yOffset / tileSize - 1;
        int y1 = (screen.yOffset + screen.height + tileSize) / tileSize + 1;

        // Clamp coordinates to the actual level bounds
        x0 = Math.max(0, x0);
        x1 = Math.min(width, x1); // Use width (exclusive)
        y0 = Math.max(0, y0);
        y1 = Math.min(height, y1); // Use height (exclusive)

        // Loop through the visible tile range and render each tile
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                // Get the tile (handles bounds checking via getTile)
                Tile tile = getTile(x, y);
                // Render the tile (Tile.render handles null sprites)
                tile.render(x, y, screen, tileSize);
            }
        }
    }

    /**
     * Sets the Tile object at the specified tile coordinates.
     * Called by MapLoader.
     * @param x Tile x-coordinate.
     * @param y Tile y-coordinate.
     * @param tile The Tile object to place at this location.
     */
    public void setTile(int x, int y, Tile tile) {
        if (x < 0 || y < 0 || x >= width || y >= height || tile == null) {
            // System.err.printf("Warning: Attempted to set invalid tile at (%d, %d) or null tile.%n", x, y);
            return; // Ignore invalid coordinates or null tile
        }
        levelTiles[y][x] = tile;

        // --- Store Player Spawn Point ---
        // Check if this tile was the designated spawn point color
        // NOTE: Assumes Tile class stores original mapColor or has a specific SPAWN type
        if (tile.mapColor == Tile.COLOR_SPAWN_POINT && playerSpawnX == -1) { // Store first spawn point found
            // Store pixel coordinates (e.g., center of the tile)
            this.playerSpawnX = x * tileSize + tileSize / 2;
            this.playerSpawnY = y * tileSize + tileSize / 2;
            System.out.printf("Stored player spawn at pixel (%d, %d) from tile (%d, %d)%n", playerSpawnX, playerSpawnY, x, y);
        }
        // ---------------------------------
    }

    /**
     * Gets the Tile object at the specified tile coordinates.
     * Handles bounds checking, returning VOID for invalid coordinates.
     * @param x Tile x-coordinate.
     * @param y Tile y-coordinate.
     * @return The Tile object at that location, or Tile.VOID if out of bounds or null.
     */
    public Tile getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return Tile.VOID; // Return the static VOID tile instance for out-of-bounds
        }
        Tile tile = levelTiles[y][x];
        // Return void if the tile wasn't loaded properly (safeguard)
        return (tile != null) ? tile : Tile.VOID;
    }

    /**
     * Checks if the tile at the specified coordinates is solid by asking the Tile object.
     * @param x Tile x-coordinate.
     * @param y Tile y-coordinate.
     * @return True if the tile is solid, false otherwise.
     */
    public boolean isSolid(int x, int y) {
        // Get the tile object (handles bounds checking) and return its solidity
        return getTile(x, y).isSolid();
    }

    // --- Getters ---
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTileSize() { return tileSize; }

    // --- Optional: Getters for spawn point ---
    // Returns -1 if no spawn point color was found in the map
    public int getPlayerSpawnX() { return playerSpawnX; }
    public int getPlayerSpawnY() { return playerSpawnY; }
}
