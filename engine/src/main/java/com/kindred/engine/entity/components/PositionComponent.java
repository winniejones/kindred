package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class PositionComponent implements Component {
    public int x, y;
    public PositionComponent(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
