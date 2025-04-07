package com.kindred.engine.level;

import com.kindred.engine.render.Screen;
import com.kindred.engine.resource.AssetLoader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.awt.Color; // Import Color for fallback sprite
import java.awt.Graphics2D; // Import Graphics2D for fallback sprite

@Slf4j
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
    
    // Spawn Marker Colors (Unique colors not used for terrain):
    public static final int COLOR_PLAYER_SPAWN = 0xFFFF0000; // Example: Red Spawn Point
    public static final int COLOR_NPC_SPAWN = 0xFF00FF01; // Example: Green for NPC
    public static final int COLOR_ENEMY_SPAWN = 0xFFFF00FE; // Example: Magenta for Enemy


    // --- Load Spritesheets ---
    // TODO: Update these paths to your actual spritesheet locations
    private static final BufferedImage tileSheet = AssetLoader.loadImage("/assets/sheets/spawn_sprites.png"); // Example path

    // Add other sheets if needed, e.g., for spawn level specific tiles
    // private static final BufferedImage spawnSheet = AssetLoader.loadImage("/assets/sheets/spawn_sheet.png");

    // --- Static Tile Instances ---
    // Load sprites using AssetLoader.getSprite(sheet, col, row, width, height)

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

     // NOTE: We don't need specific Tile instances for spawn points,
     // MapLoader detects the color and places a regular floor tile.


    // --- Instance Variables ---
    public final BufferedImage sprite; // Sprite for rendering this tile (can be placeholder)
    /**
     * -- GETTER --
     * Checks if solid
     */
    @Getter
    public final boolean solid;      // Does this tile block movement?
    public final int mapColor;       // The ARGB color used in the map image file for this tile type
    /**
     * -- GETTER --
     * Checks if buildable
     */
    @Getter
    public final boolean buildable;  // Can structures be built on this tile type?

    /** Constructor */
    public Tile(BufferedImage sprite, boolean solid, int mapColor, boolean buildable) {
        // Use the provided sprite, or if null/placeholder, create a colored square fallback
        if (sprite == null || sprite.getWidth() <= 1) { // Check if AssetLoader returned null or placeholder
            this.sprite = createDefaultSprite(TILE_WIDTH, TILE_HEIGHT, mapColor);
            log.error("Warning: Using fallback color sprite for tile with mapColor: 0x" + Integer.toHexString(mapColor));
        } else {
            this.sprite = sprite;
        }
        this.solid = solid;
        this.mapColor = mapColor;
        this.buildable = buildable;
    }

    /** Renders this tile */
    public void render(int x, int y, Screen screen, int tileSize) {
        int screenX = x * tileSize;
        int screenY = y * tileSize;
        screen.drawSpriteWithOffset(screenX, screenY, this.sprite);
    }

    /** Static helper method to get a Tile object based on a map color. */
    public static Tile getTileFromColor(int mapColor) {
        // IMPORTANT: This method should ONLY return terrain/wall tiles.
        // Marker colors (spawn points) are handled separately in MapLoader.
        if (mapColor == COLOR_WALL_BLACK) {
            return WALL;
        } else if (mapColor == COLOR_FLOOR_GRAY) {
            return FLOOR;
        } else if (mapColor == COLOR_GRASS) {
             // Warning: This color might be used as NPC spawn marker in example
            return GRASS;
        } else if (mapColor == COLOR_WATER) {
            return WATER;
        }
        // Add more terrain tile types here

        // If it's not a known terrain color (and not transparent/void), return default
        // Check alpha before warning: (mapColor >>> 24) != 0
        if ((mapColor >>> 24) != 0 && mapColor != COLOR_VOID) {
             // This will also catch unhandled spawn marker colors if MapLoader logic fails
             log.error("Warning: Unknown terrain map color encountered: 0x" + Integer.toHexString(mapColor) + ". Returning VOID.");
        }
        return VOID; // Default to VOID for unknown colors or marker colors
    }

    /** Creates a simple fallback sprite */
    private static BufferedImage createDefaultSprite(int width, int height, int color) {
        BufferedImage img = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(color, true)); // Use ARGB color
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }
}
