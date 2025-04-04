package com.kindred.engine.level;

import com.kindred.engine.render.Screen;

public class Level {

    private final int width, height;
    private final int tileSize;
    private final int[][] tiles;

    public Level(int width, int height, int tileSize) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.tiles = new int[height][width];
    }

    public void generateTestMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = (x + y) % 2 == 0 ? 0x333333 : 0x444444;
            }
        }
    }

    public void render(Screen screen) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                screen.fillRect(x * tileSize, y * tileSize, tileSize, tileSize, tiles[y][x], true);
            }
        }
    }

    public int getTileColor(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return 0;
        return tiles[y][x];
    }

    public boolean isSolid(int x, int y) {
        // Placeholder: no solid tiles yet
        return false;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileSize() {
        return tileSize;
    }
}
