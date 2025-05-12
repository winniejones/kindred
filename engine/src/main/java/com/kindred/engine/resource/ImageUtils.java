package com.kindred.engine.resource;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImageUtils {
    /**
     * Rotates a BufferedImage by a given angle (in degrees) around its center.
     * The resulting image will be sized to fit the rotated content.
     *
     * @param image The BufferedImage to rotate.
     * @param angle The angle of rotation in degrees (clockwise).
     * @return A new BufferedImage containing the rotated image, or null if the input is null.
     */
    public static BufferedImage rotateImage(BufferedImage image, double angle) {
        if (image == null) {
            return null;
        }

        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        // Calculate the new dimensions of the bounding box for the rotated image
        int newWidth = (int) Math.floor(originalWidth * cos + originalHeight * sin);
        int newHeight = (int) Math.floor(originalHeight * cos + originalWidth * sin);

        // Ensure new dimensions are at least 1x1
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);

        // Create a new BufferedImage with transparency
        // Using TYPE_INT_ARGB ensures that transparency is handled correctly.
        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();

        // Create an AffineTransform for the rotation
        AffineTransform at = new AffineTransform();

        // 1. Translate the image so its center is at the origin (0,0)
        //    Then, translate it to the center of the new, larger canvas.
        at.translate((newWidth - originalWidth) / 2.0, (newHeight - originalHeight) / 2.0);

        // 2. Rotate around the center of the original image's position within the new canvas
        at.rotate(radians, originalWidth / 2.0, originalHeight / 2.0);

        // Apply the transform and draw the original image
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
    }

    /**
     * Extracts a single frame from a vertical sprite sheet.
     *
     * @param spriteSheet The full sprite sheet.
     * @param frameIndex  The 0-based index of the frame to extract.
     * @param frameWidth  The width of a single frame.
     * @param frameHeight The height of a single frame.
     * @return The extracted frame as a BufferedImage, or a placeholder if extraction fails.
     */
    public static BufferedImage extractFrame(BufferedImage spriteSheet, int frameIndex, int frameWidth, int frameHeight) {
        if (spriteSheet == null) {
            return AssetLoader.createPlaceholderImage(frameWidth, frameHeight);
        }
        int yOffset = frameIndex * frameHeight;
        if (yOffset + frameHeight > spriteSheet.getHeight() || frameWidth > spriteSheet.getWidth()) {
            System.err.println("Error extracting frame " + frameIndex + ": Coordinates out of bounds.");
            return AssetLoader.createPlaceholderImage(frameWidth, frameHeight);
        }
        return spriteSheet.getSubimage(0, yOffset, frameWidth, frameHeight);
    }
}
