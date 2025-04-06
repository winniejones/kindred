package com.kindred.engine.render;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

public class Screen {

    public int width, height;
    public int[] pixels;
    public final int MAP_SIZE = 64;
    public final int MAP_SIZE_MASK = MAP_SIZE - 1;
    public int xOffset, yOffset;
    public int[] tiles = new int[MAP_SIZE * MAP_SIZE];
    private final Random random = new Random();

    private final int ALPHA_COL = 0xffff00ff;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];

        for (int i = 0; i < MAP_SIZE * MAP_SIZE; i++) {
            tiles[i] = random.nextInt(0xffffff);
            tiles[0] = 0;
        }
    }

    public void clear() {
        Arrays.fill(pixels, 0);
    }

    public void setOffset(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public void drawRect(int xp, int yp, int width, int height, int color, boolean fixed) {
        if (fixed) {
            xp -= xOffset;
            yp -= yOffset;
        }
        for (int x = xp; x < xp + width; x++) {
            if (x < 0 | x >= this.width || yp >= this.height) continue;
            if (yp > 0) pixels[x + yp * this.width] = color;
            if (yp + height >= this.height) continue;
            if (yp + height > 0) pixels[x + (yp + height) * this.width] = color;
        }
        for (int y = yp; y <= yp + height; y++) {
            if (xp >= this.width || y < 0 || y >= this.height) continue;
            if (xp > 0) pixels[xp + y * this.width] = color;
            if (xp + width >= this.width) continue;
            if (xp + width > 0) pixels[(xp + width) + y * this.width] = color;
        }
    }

    public void fillRect(int xp, int yp, int width, int height, int color, boolean fixed) {
        if (fixed) {
            xp -= xOffset;
            yp -= yOffset;
        }

        for (int y = 0; y < height; y++) {
            int yo = yp + y;
            if (yo < 0 || yo >= this.height)
                continue;
            for (int x = 0; x < width; x++) {
                int xo = xp + x;
                if (xo < 0 || xo >= this.width)
                    continue;
                pixels[xo + yo * this.width] = color;
            }
        }
    }

    public void drawBackground(int tileSize, int worldWidth, int worldHeight) {
        for (int y = 0; y < worldHeight; y++) {
            for (int x = 0; x < worldWidth; x++) {
                fillRect(x * tileSize, y * tileSize, tileSize, tileSize, 0x222222, true); // Use camera offset
            }
        }
    }

    public void drawSprite(int xp, int yp, BufferedImage sprite) {
        int spriteWidth = sprite.getWidth();
        int spriteHeight = sprite.getHeight();
        for (int y = 0; y < spriteHeight; y++) {
            int ya = y + yp;
            if (ya < 0 || ya >= height) continue;
            for (int x = 0; x < spriteWidth; x++) {
                int xa = x + xp;
                if (xa < 0 || xa >= width) continue;
                int col = sprite.getRGB(x, y);
                if ((col >> 24) == 0) continue; // skip transparent pixels
                pixels[xa + ya * width] = col;
            }
        }
    }
    public void drawSpriteWithAlpha(int xp, int yp, BufferedImage sprite) {
        xp -= xOffset;
        yp -= yOffset;
        for (int y = 0; y < sprite.getHeight(); y++) {
            int ya = y + yp;
            if (ya < 0 || ya >= height) continue;
            for (int x = 0; x < sprite.getWidth(); x++) {
                int xa = x + xp;
                if (xa < 0 || xa >= width) continue;
                int col = sprite.getRGB(x, y);
                if (col != ALPHA_COL) {
                    pixels[xa + ya * width] = col;
                }
            }
        }
    }
    public void drawSpriteWithColorKey(int xp, int yp, BufferedImage sprite, int colorKey) {
        xp -= xOffset;
        yp -= yOffset;
        for (int y = 0; y < sprite.getHeight(); y++) {
            int ya = y + yp;
            if (ya < 0 || ya >= height) continue;
            for (int x = 0; x < sprite.getWidth(); x++) {
                int xa = x + xp;
                if (xa < 0 || xa >= width) continue;
                int col = sprite.getRGB(x, y);
                if (col != colorKey) {
                    pixels[xa + ya * width] = col;
                }
            }
        }
    }

    public void drawSpriteWithOffset(int xp, int yp, BufferedImage sprite) {
        if (sprite == null) return; // Don't draw if sprite is null

        // Apply camera offset to get screen coordinates
        xp -= xOffset;
        yp -= yOffset;

        int spriteWidth = sprite.getWidth();
        int spriteHeight = sprite.getHeight();

        // Determine the screen area to draw onto (clipping)
        int startX = Math.max(0, xp);
        int startY = Math.max(0, yp);
        int endX = Math.min(width, xp + spriteWidth);
        int endY = Math.min(height, yp + spriteHeight);

        // Determine the corresponding area to read from the sprite
        int spriteStartX = startX - xp;
        int spriteStartY = startY - yp;

        // Loop through the clipped screen coordinates
        for (int y = startY; y < endY; y++) {
            int spriteY = y - yp; // Corresponding y in sprite image
            int screenRowOffset = y * width;
            for (int x = startX; x < endX; x++) {
                int spriteX = x - xp; // Corresponding x in sprite image
                int col = sprite.getRGB(spriteX, spriteY); // Get ARGB color from sprite

                // --- Transparency Check ---
                // Check if the alpha byte is 0 (fully transparent)
                if ((col >>> 24) != 0x00) {
                    // Alternatively, use a color key: if (col != COLOR_KEY_MAGENTA) {
                    pixels[x + screenRowOffset] = col; // Draw non-transparent pixel
                }
                // --------------------------
            }
        }
    }


    // These should eventually be broken out into a separate Renderer class if complexity grows.
}