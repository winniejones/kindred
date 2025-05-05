package com.kindred.engine.ui;

import java.awt.*;

public final class UIRenderHelper {
    private UIRenderHelper() {}

    public static void drawBorder(Graphics g,
                           int x, int y,
                           int width, int height,
                           int thickness,
                           Color baseColor) {
        // derive highlight/shadow
        Color highlight = baseColor.brighter();
        Color shadow = baseColor.darker();

        drawBorder(g, x, y, width, height, thickness, highlight, shadow);
    }

    public static void drawBorder(Graphics g, int x, int y, int width, int height, int thickness, Color highlight, Color shadow) {
        for (int i = 0; i < thickness; i++) {
            // top edge (left→right)
            g.setColor(highlight);
            g.drawLine(x + i, y + i, x + width - i - 1, y + i);
            // left edge (top→bottom)
            g.drawLine(x + i, y + i, x + i, y + height - i - 1);

            // bottom edge (left→right)
            g.setColor(shadow);
            g.drawLine(x + i, y + height - i - 1, x + width - i - 1, y + height - i - 1);
            // right edge (top→bottom)
            g.drawLine(x + width - i - 1, y + i, x + width - i - 1, y + height - i - 1);
        }
    }

    public static void drawBorder(Graphics2D g2, int x, int y, int width, int height, int thickness, Color highlight, Color shadow) {
        for (int i = 0; i < thickness; i++) {
            // top edge (left→right)
            g2.setColor(highlight);
            g2.drawLine(x + i, y + i, x + width - i - 1, y + i);
            // left edge (top→bottom)
            g2.drawLine(x + i, y + i, x + i, y + height - i - 1);

            // bottom edge (left→right)
            g2.setColor(shadow);
            g2.drawLine(x + i, y + height - i - 1, x + width - i - 1, y + height - i - 1);
            // right edge (top→bottom)
            g2.drawLine(x + width - i - 1, y + i, x + width - i - 1, y + height - i - 1);
        }
    }

}
