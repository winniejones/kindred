package com.kindred.game.text;

public enum PlayerTextKey {
    DIALOGUE_EXAMPLE("dialogue.example"),
    OBSERVATION_EXAMPLE("observation.example"),
    PROMPT_EXAMPLE("prompt.example"),
    FALLBACK_EXAMPLE("fallback.example"),
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
