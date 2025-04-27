package com.kindred.engine.render;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Random;

@Slf4j
public class Screen {

    public final int width, height;
    public final int[] pixels;
    public final int MAP_SIZE = 64;
    public int xOffset, yOffset;
    private final Random random = new Random();

    private final int ALPHA_COL = 0xffff00ff;

    // --- Internal BufferedImage for Graphics context ---
    // Create a BufferedImage that WRAPS the existing pixels array.
    // This allows us to get a Graphics2D context to draw onto the buffer.
    // NOTE: This assumes the pixel format used by GameMain matches TYPE_INT_ARGB.
    // If GameMain uses TYPE_INT_RGB, text drawing might not handle alpha correctly.
    private final BufferedImage bufferImage;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];

        // --- Initialize bufferImage ---
        // 1. Create a DataBuffer using your existing pixels array
        DataBufferInt dataBuffer = new DataBufferInt(pixels, pixels.length);

        // 2. Define the color model (e.g., ARGB)
        // Masks for ARGB: Alpha (bits 24-31), Red (16-23), Green (8-15), Blue (0-7)
        DirectColorModel colorModel = new DirectColorModel(32,
                0x00ff0000, // Red mask
                0x0000ff00, // Green mask
                0x000000ff, // Blue mask
                0xff000000  // Alpha mask
        );
        // If using RGB (no alpha): new DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF);

        // 3. Create a WritableRaster using the DataBuffer and dimensions
        // The raster defines how pixels are stored and accessed.
        // SinglePixelPackedSampleModel is suitable for TYPE_INT_ARGB/RGB.
        int[] bandMasks = {colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask(), colorModel.getAlphaMask()};
        WritableRaster raster = WritableRaster.createPackedRaster(dataBuffer, width, height, width, bandMasks, null);
        // For RGB: int[] rgbMasks = {colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask()};
        // WritableRaster raster = WritableRaster.createPackedRaster(dataBuffer, width, height, width, rgbMasks, null);


        // 4. Create the BufferedImage using the ColorModel and Raster
        // This ensures the BufferedImage directly uses your 'pixels' array via the raster.
        this.bufferImage = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
        // -----------------------------

        log.info("Screen initialized. BufferedImage uses shared pixel buffer.");
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
        if (sprite == null) return;

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
        if (sprite == null) return; // Don't draw if sprite is null

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

    /**
     * Draws text directly onto the screen buffer at the specified SCREEN coordinates.
     * Handles centering and applies anti-aliasing.
     * @param screenX The target X coordinate on the screen (for horizontal centering).
     * @param screenY The target Y coordinate on the screen (baseline for vertical centering).
     * @param text The String to draw.
     * @param font The Font to use.
     * @param color The Color to use.
     * @param centered If true, centers the text horizontally at screenX. If false, screenX is the left edge.
     */
    public void drawText(int screenX, int screenY, String text, Font font, Color color, boolean centered) {
        if (text == null || text.isEmpty() || font == null || color == null) {
            log.warn("drawText called with null or empty parameters. Text: {}, Font: {}, Color: {}", text, font, color);
            return;
        }

        // Get Graphics2D context for the bufferImage (which uses our pixels array)
        Graphics2D g2d = bufferImage.createGraphics();
        if (g2d == null) {
            log.error("Failed to create Graphics2D context for text rendering!");
            return;
        }
        try {
            log.trace("drawText: Text='{}', ScreenX={}, ScreenY={}, Centered={}, Color={}, Font={}",
                    text, screenX, screenY, centered, color, font);
            // Set rendering hints for quality text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Set font and color
            g2d.setFont(font);
            g2d.setColor(color);
            FontMetrics fm = g2d.getFontMetrics();

            // Calculate drawing position
            int drawX = screenX;
            if (centered) {
                int textWidth = fm.stringWidth(text);
                drawX = screenX - textWidth / 2; // Adjust X for centering

                log.trace("drawText: Centered. TextWidth={}, DrawX={}", textWidth, drawX);
            }
            // Adjust Y to draw relative to baseline (drawString expects baseline Y)
            int drawY = screenY + fm.getAscent() / 2 - fm.getDescent()/2; // Approximate vertical center

            log.trace("drawText: Calling g2d.drawString('{}', {}, {})", text, drawX, drawY);
            // Draw the string directly onto the bufferImage's graphics context
            // This modifies the underlying 'pixels' array
            g2d.drawString(text, drawX, drawY);

        } finally {
            // IMPORTANT: Dispose the graphics context to release resources
            g2d.dispose();
        }
    }


    // These should eventually be broken out into a separate Renderer class if complexity grows.
}