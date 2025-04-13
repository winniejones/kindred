package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class AttackComponent implements Component {
    public float damage;         // How much damage the attack deals
    public float range;          // How far the attack reaches (in pixels)
    public float attackCooldown; // Time between attacks (in seconds)
    public float currentCooldown; // Time remaining until next attack is ready (in seconds)

    public AttackComponent(float damage, float range, float attackCooldown) {
        this.damage = damage;
        this.range = range;
        this.attackCooldown = attackCooldown;
        this.currentCooldown = 0; // Start ready to attack
    }
}
