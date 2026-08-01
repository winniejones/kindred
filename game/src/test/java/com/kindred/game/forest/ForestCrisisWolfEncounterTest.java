package com.kindred.game.forest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestCrisisWolfEncounterTest {

    @Test
    void warningOccursBeforeHostileContact() {
        ForestCrisisWolfEncounter encounter = createEncounter();
        WolfPlaceholder wolf = encounter.wolves().getFirst();

        GreyboxPoint warningOnlyPoint = warningOnlyPoint(wolf);

        WolfEncounterUpdate warning = encounter.update(warningOnlyPoint);

        assertEquals(WolfEncounterEvent.WARNING, warning.event());
        assertEquals(WolfState.WARNING, encounter.wolf(wolf.id()).state());
        assertFalse(encounter.canAttackPlayer(wolf.id(), warningOnlyPoint));

        WolfEncounterUpdate stillWarning = encounter.update(warningOnlyPoint);

        assertEquals(WolfEncounterEvent.NONE, stillWarning.event());
        assertEquals(WolfState.WARNING, encounter.wolf(wolf.id()).state());

        WolfEncounterUpdate hostile = encounter.update(wolf.contactArea().center());

        assertEquals(WolfEncounterEvent.CONTACT_STARTED, hostile.event());
        assertEquals(WolfState.PURSUING, encounter.wolf(wolf.id()).state());
    }

    @Test
    void warningOnlyUpdateCannotAttackOrDamage() {
        ForestCrisisWolfEncounter encounter = createEncounter();
        WolfPlaceholder wolf = encounter.wolves().getFirst();

        GreyboxPoint warningOnlyPoint = warningOnlyPoint(wolf);

        encounter.update(warningOnlyPoint);

        assertEquals(WolfState.WARNING, encounter.wolf(wolf.id()).state());
        assertFalse(encounter.canAttackPlayer(wolf.id(), warningOnlyPoint));
    }

    @Test
    void contactBeginsOnlyInsideThreatZoneAndOutsideSafePlace() {
        ForestCrisisWolfEncounter encounter = createEncounter();

        assertEquals(WolfEncounterEvent.NONE, encounter.update(encounter.greybox().playerStart()).event());
        assertEquals(WolfEncounterEvent.NONE, encounter.update(encounter.greybox().safePlaceCenter()).event());
    }

    @Test
    void wolvesPursueWithinThreatZone() {
        ForestCrisisWolfEncounter encounter = createEncounter();
        WolfPlaceholder wolf = encounter.wolves().getFirst();

        encounter.update(warningOnlyPoint(wolf));
        encounter.update(wolf.contactArea().center());

        WolfRuntimeState state = encounter.wolf(wolf.id());
        assertEquals(WolfState.PURSUING, state.state());
        assertTrue(state.position().distanceSquaredTo(wolf.spawnPosition()) <= ForestCrisisWolfEncounter.PURSUIT_STEP * ForestCrisisWolfEncounter.PURSUIT_STEP);
    }

    @Test
    void contactAreaEntryStartsPursuitWithoutBeingInAttackRange() {
        for (WolfPlaceholder wolf : createEncounter().wolves()) {
            ForestCrisisWolfEncounter encounter = createEncounter();
            GreyboxPoint contactEdge = new GreyboxPoint(wolf.contactArea().x() + 1, wolf.contactArea().y() + 1);

            encounter.update(warningOnlyPoint(wolf));
            WolfEncounterUpdate contact = encounter.update(contactEdge);

            assertEquals(WolfEncounterEvent.CONTACT_STARTED, contact.event());
            assertEquals(WolfState.PURSUING, encounter.wolf(wolf.id()).state());
            assertFalse(encounter.canAttackPlayer(wolf.id(), contactEdge));
        }
    }

    @Test
    void pursuingWolfCanLeaveOrangeYellowAndHomeAreasAcrossThreatZone() {
        for (WolfPlaceholder wolf : createEncounter().wolves()) {
            ForestCrisisWolfEncounter encounter = createEncounter();
            encounter.update(warningOnlyPoint(wolf));
            encounter.update(wolf.contactArea().center());

            GreyboxPoint target = threatPointOutsideWolfAreas(encounter.greybox(), wolf);
            for (int i = 0; i < 80; i++) {
                encounter.update(target);
            }

            GreyboxPoint wolfPosition = encounter.wolf(wolf.id()).position();
            assertEquals(WolfState.PURSUING, encounter.wolf(wolf.id()).state());
            assertTrue(encounter.greybox().isInsideThreatZone(wolfPosition));
            assertFalse(wolf.contactArea().contains(wolfPosition));
            assertFalse(wolf.warningArea().contains(wolfPosition));
            assertFalse(wolf.homeArea().contains(wolfPosition));
        }
    }

    @Test
    void remainingInsideThreatZonePreservesContactOutsideYellowAndOrange() {
        ForestCrisisWolfEncounter encounter = createEncounter();
        WolfPlaceholder wolf = encounter.wolves().getFirst();
        encounter.update(warningOnlyPoint(wolf));
        encounter.update(wolf.contactArea().center());

        WolfEncounterUpdate update = encounter.update(threatPointOutsideWolfAreas(encounter.greybox(), wolf));

        assertEquals(WolfEncounterEvent.NONE, update.event());
        assertEquals(WolfState.PURSUING, encounter.wolf(wolf.id()).state());
    }

    @Test
    void eachWolfTransitionsIndependentlyThroughEncounterStates() {
        for (WolfPlaceholder activeWolf : createEncounter().wolves()) {
            ForestCrisisWolfEncounter encounter = createEncounter();

            assertEquals(WolfState.IDLE_AT_HOME, encounter.wolf(activeWolf.id()).state());
            assertEquals(WolfEncounterEvent.WARNING, encounter.update(warningOnlyPoint(activeWolf)).event());
            assertEquals(WolfState.WARNING, encounter.wolf(activeWolf.id()).state());

            for (WolfPlaceholder otherWolf : encounter.wolves()) {
                if (!otherWolf.id().equals(activeWolf.id())) {
                    assertEquals(WolfState.IDLE_AT_HOME, encounter.wolf(otherWolf.id()).state());
                }
            }

            assertEquals(WolfEncounterEvent.CONTACT_STARTED, encounter.update(activeWolf.contactArea().center()).event());
            assertEquals(WolfState.PURSUING, encounter.wolf(activeWolf.id()).state());

            assertEquals(WolfEncounterEvent.CONTACT_BROKEN, encounter.update(encounter.greybox().playerStart()).event());
            assertEquals(WolfState.RETURNING_HOME, encounter.wolf(activeWolf.id()).state());
            assertTrue(activeWolf.homeArea().contains(encounter.wolf(activeWolf.id()).position()));
        }
    }

    @Test
    void eachWolfUsesItsOwnAuthoredHomeArea() {
        ForestCrisisWolfEncounter encounter = createEncounter();

        for (WolfPlaceholder wolf : encounter.wolves()) {
            assertTrue(wolf.homeArea().contains(encounter.wolf(wolf.id()).position()));
            assertTrue(wolf.warningArea().contains(wolf.contactArea().center()));
            assertTrue(wolf.homeArea().contains(wolf.spawnPosition()));
        }
    }

    @Test
    void leavingThreatZoneBreaksContactAndReturnsWolfTowardHome() {
        ForestCrisisWolfEncounter encounter = createEncounter();
        WolfPlaceholder wolf = encounter.wolves().getFirst();
        encounter.update(warningOnlyPoint(wolf));
        encounter.update(wolf.contactArea().center());

        WolfEncounterUpdate update = encounter.update(encounter.greybox().playerStart());

        assertEquals(WolfEncounterEvent.CONTACT_BROKEN, update.event());
        assertEquals(WolfState.RETURNING_HOME, encounter.wolf(wolf.id()).state());
        assertTrue(wolf.homeArea().contains(encounter.wolf(wolf.id()).position()));
    }

    @Test
    void safePlaceBreaksContactIndependentlyWhileInsideThreatZone() {
        ForestCrisisWolfEncounter encounter = createEncounter();
        WolfPlaceholder wolf = encounter.wolves().getFirst();
        encounter.update(warningOnlyPoint(wolf));
        encounter.update(wolf.contactArea().center());

        assertTrue(encounter.greybox().isInsideThreatZone(encounter.greybox().safePlaceCenter()));
        WolfEncounterUpdate update = encounter.update(encounter.greybox().safePlaceCenter());

        assertEquals(WolfEncounterEvent.CONTACT_BROKEN, update.event());
        assertEquals(WolfState.RETURNING_HOME, encounter.wolf(wolf.id()).state());
        assertFalse(encounter.greybox().isInsideSafePlace(encounter.wolf(wolf.id()).position()));
    }

    @Test
    void reachingSafePlaceBreaksContactAndWolvesDoNotEnterSafePlace() {
        ForestCrisisWolfEncounter encounter = createEncounter();
        WolfPlaceholder wolf = encounter.wolves().getFirst();
        encounter.update(warningOnlyPoint(wolf));
        encounter.update(wolf.contactArea().center());

        WolfEncounterUpdate update = encounter.update(encounter.greybox().safePlaceCenter());

        assertEquals(WolfEncounterEvent.CONTACT_BROKEN, update.event());
        assertFalse(encounter.greybox().isInsideSafePlace(encounter.wolf(wolf.id()).position()));
    }

    @Test
    void disengagedWolvesRemainConstrainedToHomeAreas() {
        ForestCrisisWolfEncounter encounter = createEncounter();

        for (WolfPlaceholder wolf : encounter.wolves()) {
            assertTrue(wolf.homeArea().contains(encounter.wolf(wolf.id()).position()));
            encounter.update(encounter.greybox().playerStart());
            assertTrue(wolf.homeArea().contains(encounter.wolf(wolf.id()).position()));
        }
    }

    @Test
    void defeatingWolvesUpdatesPredatorDefeatStateWithoutPrematureOutcomeLocking() {
        ForestCrisisState crisis = new ForestCrisisState();
        ForestCrisisWolfEncounter encounter = ForestCrisisWolfEncounter.createDefault(ForestCrisisGreybox.createDefault(crisis), crisis);

        encounter.recordDefeat("wolf-1");

        assertEquals(1, crisis.predatorDefeats());
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());

        encounter.recordDefeat("wolf-2");

        assertEquals(2, crisis.predatorDefeats());
        assertEquals(OutcomeReadiness.COMBAT, crisis.evaluateOutcomeReadiness());

        encounter.recordDefeat("wolf-2");
        assertEquals(2, crisis.predatorDefeats());
    }

    @Test
    void eachWolfCanBeDefeatedAndRecordedExactlyOnce() {
        ForestCrisisState crisis = new ForestCrisisState();
        ForestCrisisWolfEncounter encounter = ForestCrisisWolfEncounter.createDefault(ForestCrisisGreybox.createDefault(crisis), crisis);

        int expectedDefeats = 0;
        for (WolfPlaceholder wolf : encounter.wolves()) {
            assertEquals(WolfEncounterEvent.WOLF_DEFEATED, encounter.recordDefeat(wolf.id()).event());
            expectedDefeats++;
            assertEquals(expectedDefeats, crisis.predatorDefeats());
            assertEquals(WolfState.DEFEATED, encounter.wolf(wolf.id()).state());

            assertEquals(WolfEncounterEvent.NONE, encounter.recordDefeat(wolf.id()).event());
            assertEquals(expectedDefeats, crisis.predatorDefeats());
        }
    }

    @Test
    void defeatUpdateIncludesObservableDevelopmentProgressLogAndCombatReadiness() {
        ForestCrisisState crisis = new ForestCrisisState();
        ForestCrisisWolfEncounter encounter = ForestCrisisWolfEncounter.createDefault(ForestCrisisGreybox.createDefault(crisis), crisis);

        WolfEncounterUpdate first = encounter.recordDefeat("wolf-1");

        assertEquals("Forest Crisis Predator Defeat progress: 1/2 after wolf-1", first.developmentLogMessage().orElseThrow());
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());

        WolfEncounterUpdate second = encounter.recordDefeat("wolf-2");

        assertEquals("Forest Crisis Predator Defeat progress: 2/2 after wolf-2", second.developmentLogMessage().orElseThrow());
        assertEquals(OutcomeReadiness.COMBAT, crisis.evaluateOutcomeReadiness());
    }

    private ForestCrisisWolfEncounter createEncounter() {
        ForestCrisisState crisis = new ForestCrisisState();
        return ForestCrisisWolfEncounter.createDefault(ForestCrisisGreybox.createDefault(crisis), crisis);
    }

    private GreyboxPoint warningOnlyPoint(WolfPlaceholder wolf) {
        return new GreyboxPoint(wolf.warningArea().x() + 4, wolf.warningArea().center().y());
    }

    private GreyboxPoint threatPointOutsideWolfAreas(ForestCrisisGreybox greybox, WolfPlaceholder wolf) {
        GreyboxPoint point = new GreyboxPoint(greybox.threatZone().x() + 32, wolf.contactArea().center().y());
        if (wolf.warningArea().contains(point) || wolf.contactArea().contains(point) || wolf.homeArea().contains(point)) {
            return new GreyboxPoint(greybox.threatZone().x() + greybox.threatZone().width() - 48, wolf.contactArea().center().y());
        }
        return point;
    }
}
