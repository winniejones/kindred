package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class CameraComponent implements Component {
    public int x, y;
    public CameraComponent(int x, int y) {
        this.x = x;
        this.y = y;
    }
}