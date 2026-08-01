package com.kindred.game.dialogue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class DialogueContext {
    private final Set<DialogueIntention> conversationAllowedIntentions;
    private final Set<DialogueIntention> worldStateAllowedIntentions;

    private DialogueContext(
            Set<DialogueIntention> conversationAllowedIntentions,
            Set<DialogueIntention> worldStateAllowedIntentions) {
        this.conversationAllowedIntentions = copyOf(conversationAllowedIntentions);
        this.worldStateAllowedIntentions = copyOf(worldStateAllowedIntentions);
    }

    public static DialogueContext allowing(DialogueIntention... intentions) {
        Set<DialogueIntention> allowed = EnumSet.noneOf(DialogueIntention.class);
        Collections.addAll(allowed, intentions);
        return new DialogueContext(allowed, DialogueIntention.controlledSet());
    }

    public DialogueContext withWorldStateAllowedIntentions(Set<DialogueIntention> intentions) {
        return new DialogueContext(conversationAllowedIntentions, intentions);
    }

    boolean allows(DialogueIntention intention) {
        return conversationAllowedIntentions.contains(intention)
                && worldStateAllowedIntentions.contains(intention);
    }

    private static Set<DialogueIntention> copyOf(Set<DialogueIntention> intentions) {
        if (intentions.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(intentions));
    }
}
