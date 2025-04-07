package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class PositionComponent implements Component {
    public int x, y;
    public PositionComponent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static class HealthComponent implements Component {
        public int currentHealth;
        public int maxHealth;

        public HealthComponent (int currentHealth, int maxHealth) {
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
        }
    }
}
