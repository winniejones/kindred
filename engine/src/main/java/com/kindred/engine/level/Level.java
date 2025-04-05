package com.kindred.engine.level;

import com.kindred.engine.render.Screen;

public class Level {

    private final int width, height;
    private final int tileSize;
    private final int[][] tiles;
    private final boolean[][] solids;

    public Level(int width, int height, int tileSize) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.tiles = new int[height][width];
        this.solids = new boolean[height][width];
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

    public int getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return 0;
        return tiles[y][x];
    }
    public boolean getIsSolid(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return true;
        return solids[y][x];
    }

    public void setTile(int x, int y, int color, boolean solid) {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        tiles[y][x] = color;
        solids[y][x] = solid;
    }

    public boolean isSolid(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return true;
        }; // treat out-of-bounds as solid
        return solids[y][x];
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
