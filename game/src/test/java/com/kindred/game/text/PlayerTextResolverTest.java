package com.kindred.game.text;

import com.kindred.GameMain;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTextResolverTest {

    @Test
    void resolvesPlayerFacingTextFromStableKeys() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        assertEquals("The shepherd rushes into the village, breathless and afraid.",
                resolver.resolve(PlayerTextKey.DIALOGUE_SHEPHERD_INCITING_ATTACK));
        assertEquals("Deep pawprints. They are fresh and lead toward the forest edge.",
                resolver.resolve(PlayerTextKey.OBSERVATION_PREDATOR_TRAIL));
        assertEquals("Press E to interact.", resolver.resolve(PlayerTextKey.PROMPT_INTERACT));
    }

    @Test
    void supportsEveryRequiredPlayerTextCategory() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        assertResolvesNonBlank(resolver, PlayerTextKey.DIALOGUE_SHEPHERD_INCITING_ATTACK);
        assertResolvesNonBlank(resolver, PlayerTextKey.OBSERVATION_PREDATOR_TRAIL);
        assertResolvesNonBlank(resolver, PlayerTextKey.PROMPT_INTERACT);
        assertResolvesNonBlank(resolver, PlayerTextKey.FALLBACK_UNKNOWN_INPUT);
        assertResolvesNonBlank(resolver, PlayerTextKey.CHAT_PLAYER_PREFIX);
        assertResolvesNonBlank(resolver, PlayerTextKey.TITLE_KINDRED);
        assertResolvesNonBlank(resolver, PlayerTextKey.END_CONTINUE_EXPLORING);
    }

    @Test
    void stableKeysAreSeparateFromVisibleText() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        String stableKey = PlayerTextKey.DIALOGUE_SHEPHERD_INCITING_ATTACK.key();
        String visibleText = resolver.resolve(PlayerTextKey.DIALOGUE_SHEPHERD_INCITING_ATTACK);

        assertEquals("dialogue.shepherd.incitingAttack", stableKey);
        assertNotEquals(stableKey, visibleText);
    }

    @Test
    void currentGameMainTextUsesStableTextKeys() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        assertEquals(resolver.resolve(PlayerTextKey.TITLE_KINDRED), GameMain.TITLE);
        assertEquals("You: ", resolver.resolve(PlayerTextKey.CHAT_PLAYER_PREFIX));
    }

    private void assertResolvesNonBlank(PlayerTextResolver resolver, PlayerTextKey key) {
        assertTrue(!resolver.resolve(key).isBlank());
    }
}
