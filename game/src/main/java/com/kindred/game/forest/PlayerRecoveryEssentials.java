package com.kindred.game.forest;

import com.kindred.engine.entity.components.AttackComponent;
import com.kindred.engine.entity.components.ColliderComponent;

public record PlayerRecoveryEssentials(
        int colliderWidth,
        int colliderHeight,
        int colliderOffsetX,
        int colliderOffsetY,
        float attackDamage,
        float attackRange,
        float attackCooldown) {

    public static PlayerRecoveryEssentials defaultPlayer() {
        return new PlayerRecoveryEssentials(15, 14, 8, 15, 10f, 45f, 0.5f);
    }

    public static PlayerRecoveryEssentials from(ColliderComponent collider, AttackComponent attack) {
        return new PlayerRecoveryEssentials(
                collider.hitboxWidth,
                collider.hitboxHeight,
                collider.offsetX,
                collider.offsetY,
                attack.damage,
                attack.range,
                attack.attackCooldown);
    }
}
