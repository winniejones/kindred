package com.kindred.game.forest;

import com.kindred.game.dialogue.ShepherdIntroductionDialogue;
import com.kindred.game.text.PlayerTextKey;
import com.kindred.game.text.PlayerTextResolver;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestCrisisIntroductionPathTest {

    private final PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

    @Test
    void playerStartsInVillageNearTheShepherdIntroductionWithDiegeticInteractionHint() {
        ForestCrisisIntroductionPath path = ForestCrisisIntroductionPath.createDefault(new ForestCrisisState());

        IntroductionMoment safeMoment = path.safeMoment();
        IntroductionMoment hint = path.interactionHint();

        assertEquals(ForestCrisisPlace.VILLAGE, path.currentPlace());
        assertTrue(path.isNearShepherdIntroduction());
        assertEquals(IntroductionMoment.Kind.WORLD_DESCRIPTION, safeMoment.kind());
        assertEquals(PlayerTextKey.INTRO_VILLAGE_SAFE_MOMENT, safeMoment.textKey());
        assertEquals(IntroductionMoment.Kind.DIEGETIC_HINT, hint.kind());
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_INTERACTION_HINT, hint.textKey());
        assertTrue(resolver.resolve(hint.textKey()).contains("Press E"));
    }

    @Test
    void shepherdReportUsesExistingDialogueAndDirectsThePlayerTowardTheFarm() {
        ForestCrisisIntroductionPath path = ForestCrisisIntroductionPath.createDefault(new ForestCrisisState());

        IntroductionMoment report = path.hearShepherdReport();

        assertEquals(IntroductionMoment.Kind.DIALOGUE, report.kind());
        assertEquals(ShepherdIntroductionDialogue.createDefault().openingLine(), report.textKey());
        assertTrue(resolver.resolve(report.textKey()).contains("Shepherd's Farm"));
    }

    @Test
    void playerCanReachShepherdsFarmAndSeeVisibleAttackAftermath() {
        ForestCrisisIntroductionPath path = ForestCrisisIntroductionPath.createDefault(new ForestCrisisState());

        IntroductionMoment aftermath = path.reachShepherdsFarm();

        assertEquals(ForestCrisisPlace.SHEPHERDS_FARM, path.currentPlace());
        assertEquals(IntroductionMoment.Kind.WORLD_DESCRIPTION, aftermath.kind());
        assertEquals(PlayerTextKey.SHEPHERD_FARM_ATTACK_AFTERMATH, aftermath.textKey());
        assertTrue(resolver.resolve(aftermath.textKey()).contains("fence"));
    }

    @Test
    void examiningFirstPredatorTrailShowsLowKeyObservationTextAndRecordsOnlyInternalSignState() {
        ForestCrisisState crisis = new ForestCrisisState();
        ForestCrisisIntroductionPath path = ForestCrisisIntroductionPath.createDefault(crisis);
        path.reachShepherdsFarm();

        IntroductionMoment observation = path.examinePredatorTrail();

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
    void predatorTrailCannotBeExaminedBeforeReachingTheFarm() {
        ForestCrisisState crisis = new ForestCrisisState();
        ForestCrisisIntroductionPath path = ForestCrisisIntroductionPath.createDefault(crisis);

        path.examinePredatorTrail();

        assertFalse(crisis.hasDiscovered(EnvironmentalSign.PREDATOR_TRAIL));
        assertEquals(OutcomeReadiness.NONE, crisis.evaluateOutcomeReadiness());
    }

    @Test
    void introductionTextKeysResolveWithoutUsingVisibleTextAsLogicIds() {
        ForestCrisisIntroductionPath path = ForestCrisisIntroductionPath.createDefault(new ForestCrisisState());

        for (PlayerTextKey key : new PlayerTextKey[]{
                path.safeMoment().textKey(),
                path.interactionHint().textKey(),
                path.hearShepherdReport().textKey(),
                path.reachShepherdsFarm().textKey(),
                path.examinePredatorTrail().textKey()}) {
            String visibleText = resolver.resolve(key);

            assertTrue(!visibleText.isBlank());
            assertFalse(visibleText.equals(key.key()));
        }
    }
}
