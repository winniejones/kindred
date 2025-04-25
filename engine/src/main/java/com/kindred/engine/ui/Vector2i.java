package com.kindred.engine.ui;

import java.util.Objects;

/**
 * Simple class representing a 2D integer vector or point.
 * Used for UI positioning and sizing.
 */
public class Vector2i {
    public int x;
    public int y;

    public Vector2i() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2i vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    public Vector2i set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2i add(Vector2i other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2i add(int val) {
        this.x += val;
        this.y += val;
        return this;
    }

    public Vector2i subtract(Vector2i other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    // Optional: Methods to return new vectors instead of modifying 'this'
    public Vector2i added(Vector2i other) {
        return new Vector2i(this.x + other.x, this.y + other.y);
    }
    public Vector2i subtracted(Vector2i other) {
        return new Vector2i(this.x - other.x, this.y - other.y);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2i vector2i = (Vector2i) o;
        return x == vector2i.x && y == vector2i.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Vector2i{" + "x=" + x + ", y=" + y + '}';
    }
}
