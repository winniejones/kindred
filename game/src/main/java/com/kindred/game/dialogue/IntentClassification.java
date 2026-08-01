package com.kindred.game.dialogue;

import com.kindred.game.text.PlayerTextKey;

import java.util.Optional;

public record IntentClassification(
        Status status,
        Optional<DialogueIntention> intention,
        PlayerTextKey fallbackKey) {

    public enum Status {
        MATCHED,
        FALLBACK
    }

    public static IntentClassification matched(DialogueIntention intention) {
        return new IntentClassification(Status.MATCHED, Optional.of(intention), null);
    }

    public static IntentClassification fallback(PlayerTextKey fallbackKey) {
        return new IntentClassification(Status.FALLBACK, Optional.empty(), fallbackKey);
    }
}
