package com.kindred.game.forest;

public record GreyboxPoint(int x, int y) {

    public float distanceSquaredTo(GreyboxPoint other) {
        int dx = other.x - x;
        int dy = other.y - y;
        return dx * dx + dy * dy;
    }
}
