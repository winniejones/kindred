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

        assertEquals("This is dialogue text.", resolver.resolve(PlayerTextKey.DIALOGUE_EXAMPLE));
        assertEquals("This is observation text.", resolver.resolve(PlayerTextKey.OBSERVATION_EXAMPLE));
        assertEquals("This is prompt text.", resolver.resolve(PlayerTextKey.PROMPT_EXAMPLE));
    }

    @Test
    void supportsEveryRequiredPlayerTextCategory() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        assertResolvesNonBlank(resolver, PlayerTextKey.DIALOGUE_EXAMPLE);
        assertResolvesNonBlank(resolver, PlayerTextKey.OBSERVATION_EXAMPLE);
        assertResolvesNonBlank(resolver, PlayerTextKey.PROMPT_EXAMPLE);
        assertResolvesNonBlank(resolver, PlayerTextKey.FALLBACK_EXAMPLE);
        assertResolvesNonBlank(resolver, PlayerTextKey.FINAL_LINE_EXAMPLE);
        assertResolvesNonBlank(resolver, PlayerTextKey.CHAT_PLAYER_PREFIX);
        assertResolvesNonBlank(resolver, PlayerTextKey.TITLE_KINDRED);
        assertResolvesNonBlank(resolver, PlayerTextKey.END_CONTINUE_EXPLORING);
    }

    @Test
    void everyDeclaredStableKeyResolvesAndIsSeparateFromVisibleText() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        for (PlayerTextKey key : PlayerTextKey.values()) {
            String visibleText = resolver.resolve(key);

            assertResolvesNonBlank(resolver, key);
            assertNotEquals(key.key(), visibleText);
        }
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
