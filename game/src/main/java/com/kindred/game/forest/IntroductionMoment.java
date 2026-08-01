package com.kindred.game.forest;

import com.kindred.game.text.PlayerTextKey;

public record IntroductionMoment(
        PlayerTextKey textKey,
        Kind kind,
        boolean createsJournalEntry,
        boolean createsChecklistEntry,
        boolean createsCounter,
        boolean createsObjectiveMarker) {

    public enum Kind {
        DIEGETIC_HINT,
        DIALOGUE,
        WORLD_DESCRIPTION,
        OBSERVATION_TEXT
    }

    public static IntroductionMoment of(PlayerTextKey textKey, Kind kind) {
        return new IntroductionMoment(textKey, kind, false, false, false, false);
    }
}
