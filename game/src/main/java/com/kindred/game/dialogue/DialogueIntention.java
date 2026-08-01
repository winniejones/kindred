package com.kindred.game.dialogue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum DialogueIntention {
    GREET,
    ASK_CAPABILITIES,
    ASK_FOR_HELP,
    CLARIFY,
    REPORT_PREDATOR_TRACKS,
    ASK_ABOUT_LOGGING,
    EXPLAIN_ECOLOGICAL_LINK,
    PROPOSE_COMPROMISE,
    CHOOSE_OBSERVATION_IMPROVEMENT,
    GOODBYE;

    public static Set<DialogueIntention> controlledSet() {
        return Collections.unmodifiableSet(EnumSet.allOf(DialogueIntention.class));
    }
}
