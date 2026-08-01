package com.kindred.game.dialogue;

import com.kindred.game.text.PlayerTextKey;

public class ShepherdIntroductionDialogue {
    private final IntentClassifier classifier;
    private final DialogueContext context;

    private ShepherdIntroductionDialogue(IntentClassifier classifier) {
        this.classifier = classifier;
        this.context = DialogueContext.allowing(
                DialogueIntention.GREET,
                DialogueIntention.ASK_CAPABILITIES,
                DialogueIntention.ASK_FOR_HELP,
                DialogueIntention.CLARIFY,
                DialogueIntention.GOODBYE);
    }

    public static ShepherdIntroductionDialogue createDefault() {
        return new ShepherdIntroductionDialogue(IntentClassifier.createDefault());
    }

    public PlayerTextKey openingLine() {
        return PlayerTextKey.SHEPHERD_INTRO_ATTACK_REPORT;
    }

    public PlayerTextKey respond(String playerInput) {
        IntentClassification classification = classifier.classify(playerInput, context);
        if (classification.status() == IntentClassification.Status.FALLBACK) {
            return classification.fallbackKey();
        }

        return switch (classification.intention().orElseThrow()) {
            case GREET -> PlayerTextKey.SHEPHERD_INTRO_GREETING;
            case ASK_CAPABILITIES -> PlayerTextKey.SHEPHERD_INTRO_CAPABILITIES;
            case ASK_FOR_HELP -> PlayerTextKey.SHEPHERD_INTRO_HELP;
            case CLARIFY -> PlayerTextKey.SHEPHERD_INTRO_CLARIFY;
            case GOODBYE -> PlayerTextKey.SHEPHERD_INTRO_GOODBYE;
            default -> PlayerTextKey.FALLBACK_INTENTION_NOT_ALLOWED;
        };
    }
}
