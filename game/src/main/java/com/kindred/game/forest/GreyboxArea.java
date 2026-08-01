package com.kindred.game.forest;

public record GreyboxArea(int x, int y, int width, int height) {

    public boolean contains(GreyboxPoint point) {
        return point.x() >= x
                && point.y() >= y
                && point.x() < x + width
                && point.y() < y + height;
    }

    public GreyboxPoint center() {
        return new GreyboxPoint(x + width / 2, y + height / 2);
    }
}
