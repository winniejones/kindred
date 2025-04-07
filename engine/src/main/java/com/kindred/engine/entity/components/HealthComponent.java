package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class HealthComponent implements Component {
    public int currentHealth;
    public int maxHealth;

    public HealthComponent (int currentHealth, int maxHealth) {
        this.currentHealth = currentHealth;
        this.maxHealth = maxHealth;
    }
}
