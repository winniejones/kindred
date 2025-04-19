package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class ParticleComponent implements Component {
    public int color;
    public int size;

    // Default simple red particle
    public ParticleComponent() {
        this.color = 0xFFFF0000; // Red
        this.size = 2;
    }

    public ParticleComponent(int color, int size) {
        this.color = color;
        this.size = Math.max(1, size); // Ensure size is at least 1
    }
}
