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
        assertTrue(greybox.isInsideThreatZone(greybox.safePlaceCenter()));
        assertFalse(greybox.isInsideSafePlace(greybox.wolfPlaceholders().getFirst().spawnPosition()));
    }

    @Test
    void wolfContactAreasAreInsideWarningAreasWithReadableMargins() {
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());

        for (WolfPlaceholder wolf : greybox.wolfPlaceholders()) {
            assertContains(wolf.warningArea(), wolf.contactArea());
            assertTrue(warningMargin(wolf) >= ForestCrisisGreybox.MIN_WARNING_MARGIN_TILES * ForestCrisisGreybox.TILE_SIZE);
        }
    }

    @Test
    void wolfWarningAreasDoNotOverlap() {
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());

        for (WolfPlaceholder first : greybox.wolfPlaceholders()) {
            for (WolfPlaceholder second : greybox.wolfPlaceholders()) {
                if (!first.id().equals(second.id())) {
                    assertFalse(overlaps(first.warningArea(), second.warningArea()));
                }
            }
        }
    }

    @Test
    void threeWolfPlaceholdersExposeSpawnHomeAreaAndEncounterCapabilities() {
        ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());

        assertEquals(3, greybox.wolfPlaceholders().size());
        assertEquals(6, greybox.markers().size());
        for (WolfPlaceholder wolf : greybox.wolfPlaceholders()) {
            assertTrue(greybox.isInsideThreatZone(wolf.spawnPosition()));
            assertTrue(greybox.isInsideThreatZone(wolf.warningArea().center()));
            assertTrue(greybox.isInsideThreatZone(wolf.contactArea().center()));
            assertTrue(wolf.homeArea().contains(wolf.spawnPosition()));
            assertTrue(wolf.warningArea().contains(wolf.contactArea().center()));
            assertTrue(wolf.hasPursuitBehavior());
            assertTrue(wolf.hasAttackBehavior());
            assertTrue(wolf.hasWarningBehavior());
        }
    }

    private void assertContains(GreyboxArea outer, GreyboxArea inner) {
        assertTrue(outer.contains(new GreyboxPoint(inner.x(), inner.y())));
        assertTrue(outer.contains(new GreyboxPoint(inner.x() + inner.width() - 1, inner.y())));
        assertTrue(outer.contains(new GreyboxPoint(inner.x(), inner.y() + inner.height() - 1)));
        assertTrue(outer.contains(new GreyboxPoint(inner.x() + inner.width() - 1, inner.y() + inner.height() - 1)));
    }

    private int warningMargin(WolfPlaceholder wolf) {
        GreyboxArea warning = wolf.warningArea();
        GreyboxArea contact = wolf.contactArea();
        int left = contact.x() - warning.x();
        int top = contact.y() - warning.y();
        int right = warning.x() + warning.width() - (contact.x() + contact.width());
        int bottom = warning.y() + warning.height() - (contact.y() + contact.height());
        return Math.min(Math.min(left, right), Math.min(top, bottom));
    }

    private boolean overlaps(GreyboxArea first, GreyboxArea second) {
        return first.x() < second.x() + second.width()
                && first.x() + first.width() > second.x()
                && first.y() < second.y() + second.height()
                && first.y() + first.height() > second.y();
    }
}
