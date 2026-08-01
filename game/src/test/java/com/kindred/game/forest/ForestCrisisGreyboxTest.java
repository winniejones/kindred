package com.kindred.game.forest;

import com.kindred.game.text.PlayerTextKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestCrisisGreyboxTest {

    @Test
    void classifiesVillageToFarmAreaTransitionsByPlayerPosition() {
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());

        assertEquals(ForestCrisisPlace.VILLAGE, greybox.placeAt(greybox.playerStart()));
        assertEquals(ForestCrisisPlace.SHEPHERDS_FARM, greybox.placeAt(greybox.shepherdsFarmApproach()));
        assertTrue(greybox.village().contains(greybox.playerStart()));
        assertTrue(greybox.shepherdsFarm().contains(greybox.shepherdsFarmApproach()));
        assertTrue(greybox.canWalkFromVillageToFarm());
    }

    @Test
    void shepherdInteractionRequiresProximity() {
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());

        assertFalse(greybox.interactAt(greybox.playerStart()).isPresent());

        IntroductionMoment report = greybox.interactAt(greybox.shepherdPosition()).orElseThrow();
        assertEquals(IntroductionMoment.Kind.DIALOGUE, report.kind());
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_ATTACK_REPORT, report.textKey());
    }

    @Test
    void predatorTrailInteractionRequiresProximityAndRecordsOnlyInternalSignState() {
        ForestCrisisState crisis = new ForestCrisisState();
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(crisis);

        assertFalse(greybox.interactAt(greybox.shepherdsFarmApproach()).isPresent());

        IntroductionMoment observation = greybox.interactAt(greybox.predatorTrailPosition()).orElseThrow();

        assertEquals(IntroductionMoment.Kind.OBSERVATION_TEXT, observation.kind());
        assertEquals(PlayerTextKey.OBSERVATION_PREDATOR_TRAIL_FIRST, observation.textKey());
        assertFalse(observation.createsJournalEntry());
        assertFalse(observation.createsChecklistEntry());
        assertFalse(observation.createsCounter());
        assertFalse(observation.createsObjectiveMarker());
        assertTrue(crisis.hasDiscovered(EnvironmentalSign.PREDATOR_TRAIL));
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());
    }

    @Test
    void classifiesThreatZoneAndSafePlace() {
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());

        assertTrue(greybox.isInsideThreatZone(greybox.threatZoneCenter()));
        assertFalse(greybox.isInsideThreatZone(greybox.playerStart()));
        assertTrue(greybox.isInsideSafePlace(greybox.safePlaceCenter()));
        assertFalse(greybox.isInsideSafePlace(greybox.threatZoneCenter()));
    }

    @Test
    void threeWolfPlaceholdersExposeSpawnAndHomeAreaDataOnly() {
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());

        assertEquals(3, greybox.wolfPlaceholders().size());
        assertEquals(6, greybox.markers().size());
        for (WolfPlaceholder wolf : greybox.wolfPlaceholders()) {
            assertTrue(greybox.isInsideThreatZone(wolf.spawnPosition()));
            assertTrue(wolf.homeArea().contains(wolf.spawnPosition()));
            assertFalse(wolf.hasPursuitBehavior());
            assertFalse(wolf.hasAttackBehavior());
            assertFalse(wolf.hasWarningBehavior());
        }
    }
}
