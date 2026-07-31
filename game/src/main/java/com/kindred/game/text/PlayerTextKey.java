package com.kindred.game.text;

public enum PlayerTextKey {
    DIALOGUE_SHEPHERD_INCITING_ATTACK("dialogue.shepherd.incitingAttack"),
    OBSERVATION_PREDATOR_TRAIL("observation.predatorTrail"),
    PROMPT_INTERACT("prompt.interact"),
    FALLBACK_UNKNOWN_INPUT("fallback.unknownInput"),
    CHAT_PLAYER_PREFIX("chat.playerPrefix"),
    TITLE_KINDRED("title.kindred"),
    END_CONTINUE_EXPLORING("end.continueExploring");

    private final String key;

    PlayerTextKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
