package com.kindred.game.text;

import com.kindred.GameMain;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        assertTrue(resolver.hasText(PlayerTextKey.DIALOGUE_SHEPHERD_INCITING_ATTACK));
        assertTrue(resolver.hasText(PlayerTextKey.OBSERVATION_PREDATOR_TRAIL));
        assertTrue(resolver.hasText(PlayerTextKey.PROMPT_INTERACT));
        assertTrue(resolver.hasText(PlayerTextKey.FALLBACK_UNKNOWN_INPUT));
        assertTrue(resolver.hasText(PlayerTextKey.FINAL_ELDER_COMBAT_REFLECTION));
        assertTrue(resolver.hasText(PlayerTextKey.TITLE_KINDRED));
        assertTrue(resolver.hasText(PlayerTextKey.END_CONTINUE_EXPLORING));
    }

    @Test
    void stableKeysAreSeparateFromVisibleText() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        String stableKey = PlayerTextKey.DIALOGUE_SHEPHERD_INCITING_ATTACK.key();
        String visibleText = resolver.resolve(PlayerTextKey.DIALOGUE_SHEPHERD_INCITING_ATTACK);

        assertEquals("dialogue.shepherd.incitingAttack", stableKey);
        assertNotEquals(stableKey, visibleText);
        assertFalse(exposesStringResolveMethod());
    }

    @Test
    void gameTitleUsesStableTextKey() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        assertEquals(resolver.resolve(PlayerTextKey.TITLE_KINDRED), GameMain.TITLE);
    }

    private boolean exposesStringResolveMethod() {
        for (Method method : PlayerTextResolver.class.getMethods()) {
            if (method.getName().equals("resolve")
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].equals(String.class)) {
                return true;
            }
        }
        return false;
    }
}
