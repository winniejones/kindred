package com.kindred.game.forest;

import com.kindred.engine.entity.components.AttackComponent;
import com.kindred.engine.entity.components.ColliderComponent;
import com.kindred.engine.entity.components.DeadComponent;
import com.kindred.engine.entity.components.ExperienceComponent;
import com.kindred.engine.entity.components.HealthComponent;
import com.kindred.engine.entity.components.LifetimeComponent;
import com.kindred.engine.entity.components.PlayerComponent;
import com.kindred.engine.entity.components.PositionComponent;
import com.kindred.engine.entity.components.StatsComponent;
import com.kindred.engine.entity.components.VelocityComponent;
import com.kindred.engine.entity.core.EntityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestCrisisRecoveryResourcesTest {

    @Test
    void startsWithApprovedResources() {
        ForestCrisisRecoveryResources resources = ForestCrisisRecoveryResources.createDefault();

        assertEquals(3, resources.bandages());
        assertEquals(2, resources.food());
    }

    @Test
    void bandageHealsImmediatelyAndDecrementsOnce() {
        ForestCrisisRecoveryResources resources = ForestCrisisRecoveryResources.createDefault();
        HealthComponent health = new HealthComponent(20, 100);

        ForestCrisisResourceUse result = resources.useBandage(health);

        assertEquals(ForestCrisisResourceEvent.BANDAGE_USED, result.event());
        assertEquals(55, health.currentHealth);
        assertEquals(2, resources.bandages());
    }

    @Test
    void bandageHealingClampsAtMaxAndNoBandageDoesNotHeal() {
        ForestCrisisRecoveryResources resources = new ForestCrisisRecoveryResources(1, 0);
        HealthComponent health = new HealthComponent(90, 100);

        resources.useBandage(health);
        ForestCrisisResourceUse empty = resources.useBandage(health);

        assertEquals(100, health.currentHealth);
        assertEquals(0, resources.bandages());
        assertEquals(ForestCrisisResourceEvent.NO_BANDAGES, empty.event());
        assertEquals(100, health.currentHealth);
    }

    @Test
    void foodHealsGraduallyOverTenSecondsAndDecrementsOnce() {
        ForestCrisisRecoveryResources resources = ForestCrisisRecoveryResources.createDefault();
        HealthComponent health = new HealthComponent(40, 100);

        ForestCrisisResourceUse started = resources.useFood(health);
        float healthBeforeTick = health.currentHealth;
        resources.update(5.0f, health);
        float halfHealed = health.currentHealth;
        resources.update(5.0f, health);

        assertEquals(ForestCrisisResourceEvent.FOOD_STARTED, started.event());
        assertEquals(40, healthBeforeTick);
        assertEquals(1, resources.food());
        assertEquals(55, halfHealed);
        assertEquals(70, health.currentHealth);
        assertFalse(resources.isFoodRecoveryActive());
    }

    @Test
    void activeFoodBlocksAnotherUseWithoutConsumptionAndHasNoPenaltyAfterEnding() {
        ForestCrisisRecoveryResources resources = new ForestCrisisRecoveryResources(0, 2);
        HealthComponent health = new HealthComponent(50, 100);

        resources.useFood(health);
        ForestCrisisResourceUse blocked = resources.useFood(health);
        resources.update(10.0f, health);
        resources.update(10.0f, health);

        assertEquals(ForestCrisisResourceEvent.FOOD_ALREADY_ACTIVE, blocked.event());
        assertEquals(1, resources.food());
        assertEquals(80, health.currentHealth);
    }

    @Test
    void foodClampsAtMaximumHealthAndNoFoodDoesNotStartRecovery() {
        ForestCrisisRecoveryResources resources = new ForestCrisisRecoveryResources(0, 1);
        HealthComponent health = new HealthComponent(90, 100);

        resources.useFood(health);
        resources.update(10.0f, health);
        ForestCrisisResourceUse empty = resources.useFood(health);

        assertEquals(100, health.currentHealth);
        assertEquals(ForestCrisisResourceEvent.NO_FOOD, empty.event());
        assertEquals(0, resources.food());
    }

    @Test
    void defeatRecoveryUsesSamePlayerEntityAtSafePlaceAndRestoresHalfHealth() {
        ForestCrisisState crisis = populatedCrisisState();
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(crisis);
        ForestCrisisRecoveryResources resources = ForestCrisisRecoveryResources.createDefault();
        EntityManager entityManager = new EntityManager();
        int player = createDefeatedPlayer(entityManager);
        ExperienceComponent experience = entityManager.getComponent(player, ExperienceComponent.class);
        experience.currentXP = 42;
        experience.currentLevel = 2;

        ForestCrisisDefeatRecovery recovery = resources.recoverDefeatedPlayer(
                entityManager,
                player,
                greybox,
                PlayerRecoveryEssentials.defaultPlayer());

        HealthComponent health = entityManager.getComponent(player, HealthComponent.class);
        PositionComponent position = entityManager.getComponent(player, PositionComponent.class);
        assertEquals(ForestCrisisResourceEvent.DEFEAT_RECOVERED, recovery.event());
        assertEquals(ForestCrisisResourceEvent.DEFEAT_COST_BANDAGE, recovery.costEvent());
        assertEquals(player, recovery.playerEntity());
        assertSame(experience, entityManager.getComponent(player, ExperienceComponent.class));
        assertEquals(greybox.safePlaceCenter().x(), position.x);
        assertEquals(greybox.safePlaceCenter().y(), position.y);
        assertEquals(50, health.currentHealth);
        assertFalse(entityManager.hasComponent(player, DeadComponent.class));
        assertFalse(entityManager.hasComponent(player, LifetimeComponent.class));
        assertNotNull(entityManager.getComponent(player, VelocityComponent.class));
        assertNotNull(entityManager.getComponent(player, ColliderComponent.class));
        assertNotNull(entityManager.getComponent(player, AttackComponent.class));
        assertNotNull(entityManager.getComponent(player, PlayerComponent.class));
        assertEquals(42, experience.currentXP);
        assertEquals(2, experience.currentLevel);
        assertTrue(crisis.hasDiscovered(EnvironmentalSign.PREDATOR_TRAIL));
        assertTrue(crisis.hasCompletedElderInterpretation());
        assertEquals(1, crisis.predatorDefeats());
        assertEquals(OutcomeReadiness.BALANCE, crisis.evaluateOutcomeReadiness());
    }

    @Test
    void safePlaceRecoveryBreaksActiveWolfContact() {
        ForestCrisisState crisis = new ForestCrisisState();
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(crisis);
        ForestCrisisWolfEncounter encounter = ForestCrisisWolfEncounter.createDefault(greybox, crisis);
        WolfPlaceholder wolf = encounter.wolves().getFirst();
        EntityManager entityManager = new EntityManager();
        int player = createDefeatedPlayer(entityManager);
        ForestCrisisRecoveryResources resources = ForestCrisisRecoveryResources.createDefault();

        encounter.update(new GreyboxPoint(wolf.warningArea().x() + 4, wolf.warningArea().center().y()));
        encounter.update(wolf.contactArea().center());
        resources.recoverDefeatedPlayer(entityManager, player, greybox, PlayerRecoveryEssentials.defaultPlayer());
        PositionComponent recoveredPosition = entityManager.getComponent(player, PositionComponent.class);
        WolfEncounterUpdate update = encounter.update(new GreyboxPoint(recoveredPosition.x, recoveredPosition.y));

        assertEquals(WolfEncounterEvent.CONTACT_BROKEN, update.event());
        assertEquals(WolfState.RETURNING_HOME, encounter.wolf(wolf.id()).state());
    }

    @Test
    void defeatConsumesBandageBeforeFoodAndNeverBelowZero() {
        EntityManager entityManager = new EntityManager();
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());
        int player = createDefeatedPlayer(entityManager);
        ForestCrisisRecoveryResources resources = new ForestCrisisRecoveryResources(1, 1);

        ForestCrisisDefeatRecovery recovery = resources.recoverDefeatedPlayer(entityManager, player, greybox, PlayerRecoveryEssentials.defaultPlayer());

        assertEquals(ForestCrisisResourceEvent.DEFEAT_COST_BANDAGE, recovery.costEvent());
        assertEquals(0, resources.bandages());
        assertEquals(1, resources.food());
    }

    @Test
    void defeatConsumesFoodOnlyWhenNoBandageAndIsSafeWhenNoResourcesExist() {
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());
        EntityManager firstManager = new EntityManager();
        int firstPlayer = createDefeatedPlayer(firstManager);
        ForestCrisisRecoveryResources withFood = new ForestCrisisRecoveryResources(0, 1);

        ForestCrisisDefeatRecovery foodCost = withFood.recoverDefeatedPlayer(firstManager, firstPlayer, greybox, PlayerRecoveryEssentials.defaultPlayer());

        EntityManager secondManager = new EntityManager();
        int secondPlayer = createDefeatedPlayer(secondManager);
        ForestCrisisRecoveryResources empty = new ForestCrisisRecoveryResources(0, 0);
        ForestCrisisDefeatRecovery noCost = empty.recoverDefeatedPlayer(secondManager, secondPlayer, greybox, PlayerRecoveryEssentials.defaultPlayer());

        assertEquals(ForestCrisisResourceEvent.DEFEAT_COST_FOOD, foodCost.costEvent());
        assertEquals(ForestCrisisResourceEvent.DEFEAT_COST_NONE, noCost.costEvent());
        assertEquals(0, withFood.bandages());
        assertEquals(0, withFood.food());
        assertEquals(0, empty.bandages());
        assertEquals(0, empty.food());
        assertFalse(empty.isEmergencyCacheClaimed());
        assertEquals(50, secondManager.getComponent(secondPlayer, HealthComponent.class).currentHealth);
    }

    @Test
    void defeatRecoveryRestoresLatestEssentialColliderAndAttackValues() {
        EntityManager entityManager = new EntityManager();
        int player = createDefeatedPlayer(entityManager);
        entityManager.removeComponent(player, ColliderComponent.class);
        entityManager.removeComponent(player, AttackComponent.class);
        PlayerRecoveryEssentials essentials = new PlayerRecoveryEssentials(21, 22, 3, 4, 12f, 52f, 0.7f);

        ForestCrisisRecoveryResources.createDefault().recoverDefeatedPlayer(
                entityManager,
                player,
                ForestCrisisGreybox.createDefault(new ForestCrisisState()),
                essentials);

        ColliderComponent collider = entityManager.getComponent(player, ColliderComponent.class);
        AttackComponent attack = entityManager.getComponent(player, AttackComponent.class);
        assertEquals(21, collider.hitboxWidth);
        assertEquals(22, collider.hitboxHeight);
        assertEquals(3, collider.offsetX);
        assertEquals(4, collider.offsetY);
        assertEquals(12f, attack.damage);
        assertEquals(52f, attack.range);
        assertEquals(0.7f, attack.attackCooldown);
    }

    @Test
    void defeatRecoveryCancelsActiveFoodSoRecoveryHealthRemainsExactlyHalf() {
        ForestCrisisRecoveryResources resources = new ForestCrisisRecoveryResources(0, 2);
        EntityManager entityManager = new EntityManager();
        int player = createDefeatedPlayer(entityManager);
        HealthComponent health = entityManager.getComponent(player, HealthComponent.class);

        resources.useFood(health);
        resources.recoverDefeatedPlayer(
                entityManager,
                player,
                ForestCrisisGreybox.createDefault(new ForestCrisisState()),
                PlayerRecoveryEssentials.defaultPlayer());
        resources.update(10.0f, health);

        assertFalse(resources.isFoodRecoveryActive());
        assertEquals(50, health.currentHealth);
        assertEquals(0, resources.food());
    }

    @Test
    void emergencyCacheGrantsResourcesExactlyOnceAndPersistsAcrossDefeat() {
        ForestCrisisRecoveryResources resources = new ForestCrisisRecoveryResources(0, 0);
        EntityManager entityManager = new EntityManager();
        int player = createDefeatedPlayer(entityManager);

        ForestCrisisResourceUse claimed = resources.claimEmergencyCache();
        resources.recoverDefeatedPlayer(
                entityManager,
                player,
                ForestCrisisGreybox.createDefault(new ForestCrisisState()),
                PlayerRecoveryEssentials.defaultPlayer());
        ForestCrisisResourceUse exhausted = resources.claimEmergencyCache();

        assertEquals(ForestCrisisResourceEvent.EMERGENCY_CACHE_CLAIMED, claimed.event());
        assertEquals(0, resources.bandages());
        assertEquals(1, resources.food());
        assertTrue(resources.isEmergencyCacheClaimed());
        assertEquals(ForestCrisisResourceEvent.EMERGENCY_CACHE_EXHAUSTED, exhausted.event());
    }

    private ForestCrisisState populatedCrisisState() {
        ForestCrisisState crisis = new ForestCrisisState();
        crisis.discoverSign(EnvironmentalSign.PREDATOR_TRAIL);
        crisis.completeElderInterpretation();
        crisis.markAlternativeLoggingArea();
        crisis.restoreDamagedGrove();
        crisis.recordPredatorDefeat();
        return crisis;
    }

    private int createDefeatedPlayer(EntityManager entityManager) {
        int player = entityManager.createEntity();
        entityManager.addComponent(player, new PositionComponent(560, 496));
        entityManager.addComponent(player, new VelocityComponent(2, 1));
        entityManager.addComponent(player, new ColliderComponent(15, 14, 8, 15));
        entityManager.addComponent(player, new AttackComponent(10f, 45f, 0.5f));
        entityManager.addComponent(player, new HealthComponent(0, 100));
        entityManager.addComponent(player, new PlayerComponent());
        entityManager.addComponent(player, new ExperienceComponent());
        entityManager.addComponent(player, new StatsComponent());
        entityManager.addComponent(player, new DeadComponent());
        entityManager.addComponent(player, new LifetimeComponent(10));
        return player;
    }
}
