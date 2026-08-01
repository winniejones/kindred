package com.kindred.game.dialogue;

import com.kindred.game.text.PlayerTextKey;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public class IntentClassifier {

    public static IntentClassifier createDefault() {
        return new IntentClassifier();
    }

    public IntentClassification classify(String input, DialogueContext context) {
        Set<DialogueIntention> matches = findMatches(normalize(input));
        if (matches.isEmpty()) {
            return IntentClassification.fallback(PlayerTextKey.FALLBACK_UNKNOWN_INPUT);
        }
        if (matches.size() > 1) {
            return IntentClassification.fallback(PlayerTextKey.FALLBACK_AMBIGUOUS_INPUT);
        }

        DialogueIntention intention = matches.iterator().next();
        if (!context.allows(intention)) {
            return IntentClassification.fallback(PlayerTextKey.FALLBACK_INTENTION_NOT_ALLOWED);
        }

        return IntentClassification.matched(intention);
    }

    private Set<DialogueIntention> findMatches(String input) {
        Set<DialogueIntention> matches = EnumSet.noneOf(DialogueIntention.class);
        if (containsAny(input, "hello", "hi", "good morning", "helo")) {
            matches.add(DialogueIntention.GREET);
        }
        if (containsAny(input, "what can you do", "what can i say", "options", "capabilities")) {
            matches.add(DialogueIntention.ASK_CAPABILITIES);
        }
        if (containsAny(input, "help", "need help", "can you help")) {
            matches.add(DialogueIntention.ASK_FOR_HELP);
        }
        if (containsAny(input, "clarify", "what do you mean", "explain that")) {
            matches.add(DialogueIntention.CLARIFY);
        }
        if (containsAny(input, "tracks", "pawprints", "wolf tracks", "predator tracks")) {
            matches.add(DialogueIntention.REPORT_PREDATOR_TRACKS);
        }
        if (isLoggingQuestion(input)) {
            matches.add(DialogueIntention.ASK_ABOUT_LOGGING);
        }
        if (containsAny(input, "cause", "because", "connected", "ecological", "chain")) {
            matches.add(DialogueIntention.EXPLAIN_ECOLOGICAL_LINK);
        }
        if (containsAny(input, "compromise", "move the logging", "logging elsewhere", "work elsewhere")) {
            matches.add(DialogueIntention.PROPOSE_COMPROMISE);
        }
        if (containsAny(input, "observation", "observe", "see what others miss")) {
            matches.add(DialogueIntention.CHOOSE_OBSERVATION_IMPROVEMENT);
        }
        if (containsAny(input, "goodbye", "bye", "farewell")) {
            matches.add(DialogueIntention.GOODBYE);
        }
        return matches;
    }

    private boolean isLoggingQuestion(String input) {
        if (!containsAny(input, "logging", "loggers", "timber")) {
            return false;
        }
        return !containsAny(input, "move", "elsewhere", "compromise");
    }

    private boolean containsAny(String input, String... terms) {
        for (String term : terms) {
            if ((" " + input + " ").contains(" " + term + " ")) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String input) {
        return input.toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", " ").replaceAll("\\s+", " ").trim();
    }
}
