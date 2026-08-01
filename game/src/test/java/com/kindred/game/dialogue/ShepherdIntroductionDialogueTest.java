package com.kindred.game.dialogue;

import com.kindred.game.text.PlayerTextKey;
import com.kindred.game.text.PlayerTextResolver;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShepherdIntroductionDialogueTest {

    private final ShepherdIntroductionDialogue dialogue = ShepherdIntroductionDialogue.createDefault();

    @Test
    void openingLineReportsTheIncitingAttackAndPointsToTheShepherdsFarm() {
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_ATTACK_REPORT, dialogue.openingLine());

        String text = PlayerTextResolver.forLocale(Locale.ENGLISH).resolve(dialogue.openingLine());
        assertTrue(text.contains("Shepherd's Farm"));
        assertTrue(text.contains("wolf"));
    }

    @Test
    void supportsBasicHelpAndClarificationIntentionsThroughFreeText() {
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_HELP, dialogue.respond("how can I help?"));
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_HELP, dialogue.respond("help"));
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_CLARIFY, dialogue.respond("what do you mean?"));
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_CLARIFY, dialogue.respond("what happened?"));
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_CLARIFY, dialogue.respond("clarify that"));
    }

    @Test
    void supportsMinimalConversationUtilityIntentions() {
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_GREETING, dialogue.respond("hello"));
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_CAPABILITIES, dialogue.respond("what can I say?"));
        assertEquals(PlayerTextKey.SHEPHERD_INTRO_GOODBYE, dialogue.respond("goodbye"));
    }

    @Test
    void returnsDiegeticFallbacksForUnknownAmbiguousAndOutOfContextInput() {
        assertEquals(PlayerTextKey.FALLBACK_UNKNOWN_INPUT, dialogue.respond("tell me about the moon temple"));
        assertEquals(PlayerTextKey.FALLBACK_AMBIGUOUS_INPUT, dialogue.respond("help logging"));
        assertEquals(PlayerTextKey.FALLBACK_INTENTION_NOT_ALLOWED, dialogue.respond("what about logging?"));
    }

    @Test
    void everyShepherdResponseUsesAStableLocalizableTextKey() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);

        for (PlayerTextKey key : new PlayerTextKey[]{
                dialogue.openingLine(),
                dialogue.respond("hello"),
                dialogue.respond("help"),
                dialogue.respond("what do you mean"),
                dialogue.respond("what can I say"),
                dialogue.respond("goodbye")}) {
            String visibleText = resolver.resolve(key);

            assertTrue(!visibleText.isBlank());
            assertNotEquals(key.key(), visibleText);
        }
    }
}
