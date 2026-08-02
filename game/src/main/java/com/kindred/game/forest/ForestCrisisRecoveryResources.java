package com.kindred.game.forest;

import com.kindred.engine.entity.components.AttackActionComponent;
import com.kindred.engine.entity.components.AttackComponent;
import com.kindred.engine.entity.components.ColliderComponent;
import com.kindred.engine.entity.components.DeadComponent;
import com.kindred.engine.entity.components.HealthComponent;
import com.kindred.engine.entity.components.LifetimeComponent;
import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.components.TookDamageComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.core.EntityManager;

public class ForestCrisisRecoveryResources {
    public static final int STARTING_BANDAGES = 3;
    public static final int STARTING_FOOD = 2;
    public static final float BANDAGE_HEALING = 35f;
    public static final float FOOD_TOTAL_HEALING = 30f;
    public static final float FOOD_DURATION_SECONDS = 10f;

    private int bandages;
    private int food;
    private boolean foodRecoveryActive;
    private float foodHealingRemaining;
    private float foodSecondsRemaining;
    private boolean emergencyCacheClaimed;

    public ForestCrisisRecoveryResources(int bandages, int food) {
        this.bandages = Math.max(0, bandages);
        this.food = Math.max(0, food);
    }

    public static ForestCrisisRecoveryResources createDefault() {
        return new ForestCrisisRecoveryResources(STARTING_BANDAGES, STARTING_FOOD);
    }

    public ForestCrisisResourceUse useBandage(HealthComponent health) {
        if (bandages <= 0) {
            return new ForestCrisisResourceUse(ForestCrisisResourceEvent.NO_BANDAGES);
        }
        bandages--;
        health.heal(BANDAGE_HEALING);
        return new ForestCrisisResourceUse(ForestCrisisResourceEvent.BANDAGE_USED);
    }

    public ForestCrisisResourceUse useFood(HealthComponent health) {
        if (foodRecoveryActive) {
            return new ForestCrisisResourceUse(ForestCrisisResourceEvent.FOOD_ALREADY_ACTIVE);
        }
        if (food <= 0) {
            return new ForestCrisisResourceUse(ForestCrisisResourceEvent.NO_FOOD);
        }
        food--;
        foodRecoveryActive = true;
        foodHealingRemaining = FOOD_TOTAL_HEALING;
        foodSecondsRemaining = FOOD_DURATION_SECONDS;
        return new ForestCrisisResourceUse(ForestCrisisResourceEvent.FOOD_STARTED);
    }

    public void update(float deltaTime, HealthComponent health) {
        if (!foodRecoveryActive || deltaTime <= 0) {
            return;
        }
        float elapsed = Math.min(deltaTime, foodSecondsRemaining);
        float healing = FOOD_TOTAL_HEALING / FOOD_DURATION_SECONDS * elapsed;
        healing = Math.min(healing, foodHealingRemaining);
        health.heal(healing);
        foodHealingRemaining -= healing;
        foodSecondsRemaining -= elapsed;
        if (foodSecondsRemaining <= 0 || foodHealingRemaining <= 0) {
            foodRecoveryActive = false;
            foodHealingRemaining = 0;
            foodSecondsRemaining = 0;
        }
    }

    public ForestCrisisDefeatRecovery recoverDefeatedPlayer(
            EntityManager entityManager,
            int playerEntity,
            ForestCrisisGreybox greybox,
            PlayerRecoveryEssentials essentials) {
        HealthComponent health = entityManager.getComponent(playerEntity, HealthComponent.class);
        PositionComponent position = entityManager.getComponent(playerEntity, PositionComponent.class);
        if (health == null || position == null) {
            return new ForestCrisisDefeatRecovery(ForestCrisisResourceEvent.NONE, ForestCrisisResourceEvent.DEFEAT_COST_NONE, playerEntity);
        }

        ForestCrisisResourceEvent costEvent = applyDefeatCost();
        cancelFoodRecovery();
        GreyboxPoint safePlace = greybox.safePlaceCenter();
        position.x = safePlace.x();
        position.y = safePlace.y();
        health.currentHealth = health.maxHealth * 0.5f;

        entityManager.removeComponent(playerEntity, DeadComponent.class);
        entityManager.removeComponent(playerEntity, LifetimeComponent.class);
        entityManager.removeComponent(playerEntity, TookDamageComponent.class);
        entityManager.removeComponent(playerEntity, AttackActionComponent.class);

        VelocityComponent velocity = entityManager.getComponent(playerEntity, VelocityComponent.class);
        if (velocity == null) {
            entityManager.addComponent(playerEntity, new VelocityComponent(0, 0));
        } else {
            velocity.vx = 0;
            velocity.vy = 0;
        }
        if (!entityManager.hasComponent(playerEntity, ColliderComponent.class)) {
            entityManager.addComponent(playerEntity, new ColliderComponent(
                    essentials.colliderWidth(),
                    essentials.colliderHeight(),
                    essentials.colliderOffsetX(),
                    essentials.colliderOffsetY()));
        }
        if (!entityManager.hasComponent(playerEntity, AttackComponent.class)) {
            entityManager.addComponent(playerEntity, new AttackComponent(
                    essentials.attackDamage(),
                    essentials.attackRange(),
                    essentials.attackCooldown()));
        }

        return new ForestCrisisDefeatRecovery(ForestCrisisResourceEvent.DEFEAT_RECOVERED, costEvent, playerEntity);
    }

    public ForestCrisisResourceUse claimEmergencyCache() {
        if (emergencyCacheClaimed) {
            return new ForestCrisisResourceUse(ForestCrisisResourceEvent.EMERGENCY_CACHE_EXHAUSTED);
        }
        emergencyCacheClaimed = true;
        bandages++;
        food++;
        return new ForestCrisisResourceUse(ForestCrisisResourceEvent.EMERGENCY_CACHE_CLAIMED);
    }

    public int bandages() {
        return bandages;
    }

    public int food() {
        return food;
    }

    public boolean isFoodRecoveryActive() {
        return foodRecoveryActive;
    }

    public boolean isEmergencyCacheClaimed() {
        return emergencyCacheClaimed;
    }

    private ForestCrisisResourceEvent applyDefeatCost() {
        if (bandages > 0) {
            bandages--;
            return ForestCrisisResourceEvent.DEFEAT_COST_BANDAGE;
        } else if (food > 0) {
            food--;
            return ForestCrisisResourceEvent.DEFEAT_COST_FOOD;
        }
        return ForestCrisisResourceEvent.DEFEAT_COST_NONE;
    }

    private void cancelFoodRecovery() {
        foodRecoveryActive = false;
        foodHealingRemaining = 0;
        foodSecondsRemaining = 0;
    }
}
