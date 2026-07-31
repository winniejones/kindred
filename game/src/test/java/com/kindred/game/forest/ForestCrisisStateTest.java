package com.kindred.game.forest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestCrisisStateTest {

    @Test
    void combatReadinessRequiresAtLeastTwoPredatorDefeatsAndNoBalanceActions() {
        ForestCrisisState crisis = new ForestCrisisState();

        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());

        crisis.recordPredatorDefeat();
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());

        crisis.recordPredatorDefeat();
        assertEquals(OutcomeReadiness.COMBAT, crisis.evaluateOutcomeReadiness());

        assertFalse(crisis.hasFinalCrisisOutcome());
    }

    @Test
    void balanceReadinessRequiresBothBalanceActionsAndTakesPriorityOverCombat() {
        ForestCrisisState crisis = new ForestCrisisState();

        crisis.markAlternativeLoggingArea();
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());

        crisis.restoreDamagedGrove();
        assertEquals(OutcomeReadiness.BALANCE, crisis.evaluateOutcomeReadiness());

        crisis.recordPredatorDefeat();
        crisis.recordPredatorDefeat();
        assertEquals(OutcomeReadiness.BALANCE, crisis.evaluateOutcomeReadiness());
        assertFalse(crisis.hasFinalCrisisOutcome());
    }

    @Test
    void oneBalanceActionWithAPredatorDefeatCreatesMixedReadinessUntilBothBalanceActionsAreComplete() {
        ForestCrisisState crisis = new ForestCrisisState();

        crisis.markAlternativeLoggingArea();
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());

        crisis.recordPredatorDefeat();
        assertEquals(OutcomeReadiness.MIXED, crisis.evaluateOutcomeReadiness());

        crisis.restoreDamagedGrove();
        assertEquals(OutcomeReadiness.BALANCE, crisis.evaluateOutcomeReadiness());
    }

    @Test
    void mixedReadinessRequiresOnePredatorDefeatAndExactlyOneBalanceAction() {
        ForestCrisisState crisis = new ForestCrisisState();

        crisis.restoreDamagedGrove();
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());

        crisis.recordPredatorDefeat();
        assertEquals(OutcomeReadiness.MIXED, crisis.evaluateOutcomeReadiness());

        crisis.recordPredatorDefeat();
        assertEquals(OutcomeReadiness.MIXED, crisis.evaluateOutcomeReadiness());
    }

    @Test
    void combatReadinessIsRemovedWhenABalanceActionIsCompleted() {
        ForestCrisisState crisis = new ForestCrisisState();

        crisis.recordPredatorDefeat();
        crisis.recordPredatorDefeat();
        assertEquals(OutcomeReadiness.COMBAT, crisis.evaluateOutcomeReadiness());

        crisis.markAlternativeLoggingArea();
        assertEquals(OutcomeReadiness.MIXED, crisis.evaluateOutcomeReadiness());
    }

    @Test
    void recordsDiscoveredSignsAndElderInterpretationWithoutLockingFinalOutcome() {
        ForestCrisisState crisis = new ForestCrisisState();

        crisis.discoverSign(EnvironmentalSign.ABANDONED_GRAZING_SITE);
        crisis.discoverSign(EnvironmentalSign.CLEARED_SHELTER);
        crisis.discoverSign(EnvironmentalSign.PREDATOR_TRAIL);
        crisis.completeElderInterpretation();

        assertTrue(crisis.hasDiscovered(EnvironmentalSign.ABANDONED_GRAZING_SITE));
        assertTrue(crisis.hasDiscovered(EnvironmentalSign.CLEARED_SHELTER));
        assertTrue(crisis.hasDiscovered(EnvironmentalSign.PREDATOR_TRAIL));
        assertTrue(crisis.hasCompletedElderInterpretation());
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());
        assertFalse(crisis.hasFinalCrisisOutcome());
    }
}
