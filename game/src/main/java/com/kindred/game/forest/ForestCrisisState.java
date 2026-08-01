package com.kindred.game.forest;

import java.util.EnumSet;
import java.util.Set;

public class ForestCrisisState {
    private int predatorDefeats;
    private boolean alternativeLoggingAreaMarked;
    private boolean damagedGroveRestored;
    private final Set<EnvironmentalSign> discoveredSigns = EnumSet.noneOf(EnvironmentalSign.class);
    private boolean elderInterpretationCompleted;

    public void recordPredatorDefeat() {
        predatorDefeats++;
    }

    public int predatorDefeats() {
        return predatorDefeats;
    }

    public void markAlternativeLoggingArea() {
        alternativeLoggingAreaMarked = true;
    }

    public void restoreDamagedGrove() {
        damagedGroveRestored = true;
    }

    public void discoverSign(EnvironmentalSign sign) {
        discoveredSigns.add(sign);
    }

    public boolean hasDiscovered(EnvironmentalSign sign) {
        return discoveredSigns.contains(sign);
    }

    public void completeElderInterpretation() {
        elderInterpretationCompleted = true;
    }

    public boolean hasCompletedElderInterpretation() {
        return elderInterpretationCompleted;
    }

    public OutcomeReadiness evaluateOutcomeReadiness() {
        int completedBalanceActions = completedBalanceActions();
        if (completedBalanceActions == 2) {
            return OutcomeReadiness.BALANCE;
        }
        if (predatorDefeats >= 1 && completedBalanceActions == 1) {
            return OutcomeReadiness.MIXED;
        }
        if (predatorDefeats >= 2 && completedBalanceActions == 0) {
            return OutcomeReadiness.COMBAT;
        }
        return OutcomeReadiness.NONE;
    }

    private int completedBalanceActions() {
        int completed = 0;
        if (alternativeLoggingAreaMarked) {
            completed++;
        }
        if (damagedGroveRestored) {
            completed++;
        }
        return completed;
    }
}
