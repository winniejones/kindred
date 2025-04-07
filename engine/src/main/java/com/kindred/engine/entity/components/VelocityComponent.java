package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class VelocityComponent implements Component {
    public int vx, vy;
    public VelocityComponent(int vx, int vy) {
        this.vx = vx;
        this.vy = vy;
    }
}
