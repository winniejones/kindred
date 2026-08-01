package com.kindred.game.text;

public enum PlayerTextKey {
    DIALOGUE_EXAMPLE("dialogue.example"),
    OBSERVATION_EXAMPLE("observation.example"),
    PROMPT_EXAMPLE("prompt.example"),
    FALLBACK_EXAMPLE("fallback.example"),
    FALLBACK_UNKNOWN_INPUT("fallback.unknownInput"),
    FALLBACK_AMBIGUOUS_INPUT("fallback.ambiguousInput"),
    FALLBACK_INTENTION_NOT_ALLOWED("fallback.intentionNotAllowed"),
    FINAL_LINE_EXAMPLE("finalLine.example"),
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
