package com.kindred.game.dialogue;

import com.kindred.game.text.PlayerTextKey;
import com.kindred.game.text.PlayerTextResolver;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntentClassifierTest {

    private final IntentClassifier classifier = IntentClassifier.createDefault();

    @Test
    void mapsNaturalPhrasesToTheSameControlledIntention() {
        DialogueContext context = DialogueContext.allowing(DialogueIntention.GREET, DialogueIntention.ASK_FOR_HELP);

        assertMatched(DialogueIntention.GREET, classifier.classify("hello", context));
        assertMatched(DialogueIntention.GREET, classifier.classify("good morning", context));
        assertMatched(DialogueIntention.GREET, classifier.classify("helo", context));
        assertMatched(DialogueIntention.ASK_FOR_HELP, classifier.classify("help", context));
        assertMatched(DialogueIntention.ASK_FOR_HELP, classifier.classify("can you help me?", context));
    }

    @Test
    void onlyReturnsControlledIntentionsFromTheBoundedSet() {
        assertEquals(Set.of(
                DialogueIntention.GREET,
                DialogueIntention.ASK_CAPABILITIES,
                DialogueIntention.ASK_FOR_HELP,
                DialogueIntention.CLARIFY,
                DialogueIntention.REPORT_PREDATOR_TRACKS,
                DialogueIntention.ASK_ABOUT_LOGGING,
                DialogueIntention.EXPLAIN_ECOLOGICAL_LINK,
                DialogueIntention.PROPOSE_COMPROMISE,
                DialogueIntention.CHOOSE_OBSERVATION_IMPROVEMENT,
                DialogueIntention.GOODBYE), DialogueIntention.controlledSet());
    }

    @Test
    void defaultClassifierCanRecognizeEveryControlledIntention() {
        DialogueContext context = DialogueContext.allowing(DialogueIntention.controlledSet().toArray(DialogueIntention[]::new));

        assertMatched(DialogueIntention.GREET, classifier.classify("hello", context));
        assertMatched(DialogueIntention.ASK_CAPABILITIES, classifier.classify("what can you do", context));
        assertMatched(DialogueIntention.ASK_FOR_HELP, classifier.classify("can you help me", context));
        assertMatched(DialogueIntention.CLARIFY, classifier.classify("what do you mean", context));
        assertMatched(DialogueIntention.REPORT_PREDATOR_TRACKS, classifier.classify("wolf tracks", context));
        assertMatched(DialogueIntention.ASK_ABOUT_LOGGING, classifier.classify("what about logging", context));
        assertMatched(DialogueIntention.EXPLAIN_ECOLOGICAL_LINK, classifier.classify("how is this connected", context));
        assertMatched(DialogueIntention.PROPOSE_COMPROMISE, classifier.classify("move the logging elsewhere", context));
        assertMatched(DialogueIntention.CHOOSE_OBSERVATION_IMPROVEMENT, classifier.classify("choose observation", context));
        assertMatched(DialogueIntention.GOODBYE, classifier.classify("goodbye", context));
    }

    @Test
    void doesNotMatchIntentionTermsInsideOtherWords() {
        DialogueContext context = DialogueContext.allowing(DialogueIntention.GREET);

        IntentClassification result = classifier.classify("this is not a greeting", context);

        assertEquals(IntentClassification.Status.FALLBACK, result.status());
        assertEquals(PlayerTextKey.FALLBACK_UNKNOWN_INPUT, result.fallbackKey());
    }

    @Test
    void gatesKnownIntentionsByConversationContext() {
        DialogueContext context = DialogueContext.allowing(DialogueIntention.GREET);

        IntentClassification result = classifier.classify("I found wolf tracks", context);

        assertEquals(IntentClassification.Status.FALLBACK, result.status());
        assertEquals(PlayerTextKey.FALLBACK_INTENTION_NOT_ALLOWED, result.fallbackKey());
        assertFalse(result.intention().isPresent());
    }

    @Test
    void gatesKnownIntentionsByWorldState() {
        DialogueContext context = DialogueContext.allowing(DialogueIntention.PROPOSE_COMPROMISE)
                .withWorldStateAllowedIntentions(Set.of());

        IntentClassification result = classifier.classify("we should move the logging elsewhere", context);

        assertEquals(IntentClassification.Status.FALLBACK, result.status());
        assertEquals(PlayerTextKey.FALLBACK_INTENTION_NOT_ALLOWED, result.fallbackKey());
    }

    @Test
    void returnsDiegeticFallbacksForUnknownAndAmbiguousInput() {
        DialogueContext context = DialogueContext.allowing(DialogueIntention.ASK_FOR_HELP, DialogueIntention.ASK_ABOUT_LOGGING);

        IntentClassification unknown = classifier.classify("tell me about the moon temple", context);
        IntentClassification ambiguous = classifier.classify("help logging", context);

        assertEquals(IntentClassification.Status.FALLBACK, unknown.status());
        assertEquals(PlayerTextKey.FALLBACK_UNKNOWN_INPUT, unknown.fallbackKey());
        assertEquals(IntentClassification.Status.FALLBACK, ambiguous.status());
        assertEquals(PlayerTextKey.FALLBACK_AMBIGUOUS_INPUT, ambiguous.fallbackKey());
    }

    @Test
    void fallbackResponsesResolveThroughStableTextKeys() {
        PlayerTextResolver resolver = PlayerTextResolver.forLocale(Locale.ENGLISH);
        DialogueContext context = DialogueContext.allowing(DialogueIntention.GREET);

        IntentClassification result = classifier.classify("tell me about the moon temple", context);

        assertTrue(!resolver.resolve(result.fallbackKey()).isBlank());
    }

    private void assertMatched(DialogueIntention expected, IntentClassification result) {
        assertEquals(IntentClassification.Status.MATCHED, result.status());
        assertEquals(expected, result.intention().orElseThrow());
    }
}
