package com.kindred.game.forest;

import com.kindred.game.dialogue.ShepherdIntroductionDialogue;
import com.kindred.game.text.PlayerTextKey;

public class ForestCrisisIntroductionPath {
    private final ForestCrisisState crisis;
    private final ShepherdIntroductionDialogue shepherdDialogue;
    private ForestCrisisPlace currentPlace = ForestCrisisPlace.VILLAGE;

    private ForestCrisisIntroductionPath(ForestCrisisState crisis, ShepherdIntroductionDialogue shepherdDialogue) {
        this.crisis = crisis;
        this.shepherdDialogue = shepherdDialogue;
    }

    public static ForestCrisisIntroductionPath createDefault(ForestCrisisState crisis) {
        return new ForestCrisisIntroductionPath(crisis, ShepherdIntroductionDialogue.createDefault());
    }

    public ForestCrisisPlace currentPlace() {
        return currentPlace;
    }

    public boolean isNearShepherdIntroduction() {
        return currentPlace == ForestCrisisPlace.VILLAGE;
    }

    public IntroductionMoment safeMoment() {
        return IntroductionMoment.of(PlayerTextKey.INTRO_VILLAGE_SAFE_MOMENT, IntroductionMoment.Kind.WORLD_DESCRIPTION);
    }

    public IntroductionMoment interactionHint() {
        return IntroductionMoment.of(PlayerTextKey.SHEPHERD_INTRO_INTERACTION_HINT, IntroductionMoment.Kind.DIEGETIC_HINT);
    }

    public IntroductionMoment hearShepherdReport() {
        return IntroductionMoment.of(shepherdDialogue.openingLine(), IntroductionMoment.Kind.DIALOGUE);
    }

    public IntroductionMoment reachShepherdsFarm() {
        currentPlace = ForestCrisisPlace.SHEPHERDS_FARM;
        return IntroductionMoment.of(PlayerTextKey.SHEPHERD_FARM_ATTACK_AFTERMATH, IntroductionMoment.Kind.WORLD_DESCRIPTION);
    }

    public IntroductionMoment examinePredatorTrail() {
        if (currentPlace != ForestCrisisPlace.SHEPHERDS_FARM) {
            return IntroductionMoment.of(PlayerTextKey.SHEPHERD_INTRO_INTERACTION_HINT, IntroductionMoment.Kind.DIEGETIC_HINT);
        }

        crisis.discoverSign(EnvironmentalSign.PREDATOR_TRAIL);
        return IntroductionMoment.of(PlayerTextKey.OBSERVATION_PREDATOR_TRAIL_FIRST, IntroductionMoment.Kind.OBSERVATION_TEXT);
    }
}
