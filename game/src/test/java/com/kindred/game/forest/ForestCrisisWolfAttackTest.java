package com.kindred.game.forest;

import com.kindred.engine.entity.components.AttackActionComponent;
import com.kindred.engine.entity.components.AttackComponent;
import com.kindred.engine.entity.components.ColliderComponent;
import com.kindred.engine.entity.components.EnemyComponent;
import com.kindred.engine.entity.components.HealthComponent;
import com.kindred.engine.entity.components.PlayerComponent;
import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.systems.CombatSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestCrisisWolfAttackTest {

    @Test
    void wolfAtPursuedPlayerCenterCanDamagePlayer() {
        EntityManager entityManager = new EntityManager();
        int player = entityManager.createEntity();
        entityManager.addComponent(player, new PositionComponent(120, 120));
        entityManager.addComponent(player, new ColliderComponent(15, 14, 8, 15));
        HealthComponent playerHealth = new HealthComponent(100);
        entityManager.addComponent(player, playerHealth);
        entityManager.addComponent(player, new PlayerComponent());

        int wolf = entityManager.createEntity();
        entityManager.addComponent(wolf, new PositionComponent(135, 142));
        entityManager.addComponent(wolf, new ColliderComponent(16, 16));
        entityManager.addComponent(wolf, new EnemyComponent());
        entityManager.addComponent(wolf, new AttackComponent(4f, ForestCrisisWolfEncounter.WOLF_ATTACK_RANGE, 1.0f));
        entityManager.addComponent(wolf, new AttackActionComponent());

        new CombatSystem(entityManager).update(1.0f / 60.0f);

        assertTrue(playerHealth.currentHealth < playerHealth.maxHealth);
    }
}
