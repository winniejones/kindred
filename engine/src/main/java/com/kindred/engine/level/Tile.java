package com.kindred.engine.level;

import com.kindred.engine.render.Screen;
import com.kindred.engine.resource.AssetLoader;

import java.awt.image.BufferedImage;

public class Tile {

    // --- Configuration ---
    private static final int TILE_WIDTH = 16;  // Standard width of tiles in pixels
    private static final int TILE_HEIGHT = 16; // Standard height of tiles in pixels

    // --- Map Color Keys (ARGB Hex) ---
    // IMPORTANT: Adjust these values to EXACTLY match the colors in your map image!
    public static final int COLOR_VOID = 0xFF0000FF;        // Example: Blue (used for out-of-bounds)
    public static final int COLOR_FLOOR_GRAY = 0xFF808080;  // Example: Gray Floor
    public static final int COLOR_WALL_BLACK = 0xFF000000;  // Example: Black Wall
    public static final int COLOR_GRASS = 0xFF00FF00;       // Example: Green Grass
    public static final int COLOR_WATER = 0xFF00FFFF;       // Example: Cyan Water
    public static final int COLOR_SPAWN_POINT = 0xFFFF0000; // Example: Red Spawn Point

    // --- Load Spritesheets ---
    // TODO: Update these paths to your actual spritesheet locations
    private static final BufferedImage tileSheet = AssetLoader.loadImage("/assets/sheets/spawn_sprites.png"); // Example path

    // Add other sheets if needed, e.g., for spawn level specific tiles
    // private static final BufferedImage spawnSheet = AssetLoader.loadImage("/assets/sheets/spawn_sheet.png");

    // --- Static Tile Instances ---
    // Load sprites using AssetLoader.getSprite(sheet, col, row, width, height)
    // Provide default/fallback values if loading fails

    public static final Tile VOID = new Tile(
            createDefaultSprite(TILE_WIDTH, TILE_HEIGHT, COLOR_VOID), // Use fallback color sprite for void
            true,  // solid
            COLOR_VOID,
            false // buildable
    );

    public static final Tile FLOOR = new Tile(
            AssetLoader.getSprite(tileSheet, 0, 2, TILE_WIDTH, TILE_HEIGHT), // Example: Floor at (0, 2) on sheet
            false, // solid
            COLOR_FLOOR_GRAY,
            true   // buildable
    );

    public static final Tile WALL = new Tile(
            AssetLoader.getSprite(tileSheet, 0, 1, TILE_WIDTH, TILE_HEIGHT), // Example: Wall at (0, 1) on sheet
            true,  // solid
            COLOR_WALL_BLACK,
            false  // buildable
    );

    public static final Tile GRASS = new Tile(
            AssetLoader.getSprite(tileSheet, 0, 0, TILE_WIDTH, TILE_HEIGHT), // Example: Grass at (0, 0) on sheet
            false, // solid
            COLOR_GRASS,
            true   // buildable
    );

    public static final Tile WATER = new Tile(
            AssetLoader.getSprite(tileSheet, 2, 0, TILE_WIDTH, TILE_HEIGHT), // Example: Water at (2, 0) on sheet
            false, // solid - typically walkable but maybe slows? Handled elsewhere.
            COLOR_WATER,
            false  // buildable
    );

    // Spawn point tile - uses floor sprite but has unique color for detection
    public static final Tile SPAWN_POINT_MARKER = new Tile(
            AssetLoader.getSprite(tileSheet, 0, 2, TILE_WIDTH, TILE_HEIGHT), // Use floor sprite visually
            false, // solid
            COLOR_SPAWN_POINT, // Use spawn color for detection in MapLoader
            true   // buildable (usually spawn points are on buildable ground)
    );


    // --- Instance Variables ---
    public final BufferedImage sprite; // Sprite for rendering this tile (can be placeholder)
    public final boolean solid;      // Does this tile block movement?
    public final int mapColor;       // The ARGB color used in the map image file for this tile type
    public final boolean buildable;  // Can structures be built on this tile type?

    /**
     * Constructor for a Tile type. Handles null sprites by assigning a fallback.
     * @param sprite The BufferedImage sprite for this tile (can be null or placeholder from AssetLoader).
     * @param solid True if the tile blocks movement, false otherwise.
     * @param mapColor The ARGB integer color representing this tile in the map image file.
     * @param buildable True if structures can be built on this tile type.
     */
    public Tile(BufferedImage sprite, boolean solid, int mapColor, boolean buildable) {
        // Use the provided sprite, or if null/placeholder, create a colored square fallback
        if (sprite == null || sprite.getWidth() <= 1) { // Check if AssetLoader returned null or placeholder
            this.sprite = createDefaultSprite(TILE_WIDTH, TILE_HEIGHT, mapColor);
            System.err.println("Warning: Using fallback color sprite for tile with mapColor: 0x" + Integer.toHexString(mapColor));
        } else {
            this.sprite = sprite;
        }
        this.solid = solid;
        this.mapColor = mapColor;
        this.buildable = buildable;
    }

    /**
     * Renders this tile at the specified TILE coordinates (not pixel coordinates).
     * Delegates the actual drawing to the Screen object.
     *
     * @param x Tile x-coordinate.
     * @param y Tile y-coordinate.
     * @param screen The Screen object to render onto.
     * @param tileSize The size of one tile in pixels (passed in case needed, though TILE_WIDTH/HEIGHT used here).
     */
    public void render(int x, int y, Screen screen, int tileSize) {
        // Calculate pixel coordinates for the top-left of the tile
        int screenX = x * tileSize;
        int screenY = y * tileSize;

        // Use the Screen method that handles sprites and camera offset
        // This assumes drawSpriteWithOffset handles null sprites gracefully,
        // or we rely on the constructor assigning a non-null fallback sprite.
        screen.drawSpriteWithOffset(screenX, screenY, this.sprite);
    }

    /** Checks if this tile type is solid. */
    public boolean isSolid() {
        return solid;
    }

    /** Checks if this tile type is buildable. */
    public boolean isBuildable() {
        return buildable;
    }

    /**
     * Static helper method to get a Tile object based on a map color.
     * This is used by MapLoader to translate pixels to Tile types.
     *
     * @param mapColor The ARGB color read from the map image.
     * @return The corresponding static Tile instance, or VOID if no match.
     */
    public static Tile getTileFromColor(int mapColor) {
        // Use if-else if or switch for lookup
        if (mapColor == COLOR_WALL_BLACK) {
            return WALL;
        } else if (mapColor == COLOR_FLOOR_GRAY) {
            return FLOOR;
        } else if (mapColor == COLOR_GRASS) {
            return GRASS;
        } else if (mapColor == COLOR_WATER) {
            return WATER;
        } else if (mapColor == COLOR_SPAWN_POINT) {
            // Return a specific marker or the tile it should look like
            return SPAWN_POINT_MARKER; // Use the marker tile instance
        }
        // Add more tile types here based on their colors

        // Default case if no specific color matches
        if (mapColor != COLOR_VOID && (mapColor >>> 24) != 0) { // Avoid spamming for expected void or transparent areas
            // System.err.println("Warning: Unknown map color encountered: 0x" + Integer.toHexString(mapColor));
        }
        return VOID; // Default to VOID for unknown or out-of-bounds colors
    }

    /**
     * Creates a simple fallback sprite filled with a specific color.
     * Used when the intended sprite fails to load.
     */
    private static BufferedImage createDefaultSprite(int width, int height, int color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(new java.awt.Color(color, true)); // Use ARGB color
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }
}
