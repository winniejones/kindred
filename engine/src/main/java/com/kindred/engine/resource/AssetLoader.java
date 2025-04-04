package com.kindred.engine.resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class AssetLoader {

    /**
     * Loads an image from the classpath (e.g. /assets/sprites/player.png)
     */
    public static BufferedImage loadImage(String path) {
        try (InputStream is = AssetLoader.class.getResourceAsStream(path)) {
            if (is == null) throw new IOException("Asset not found: " + path);
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
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
}
