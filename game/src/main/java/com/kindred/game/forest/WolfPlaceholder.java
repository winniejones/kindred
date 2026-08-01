package com.kindred.game.forest;

public record WolfPlaceholder(String id, GreyboxPoint spawnPosition, GreyboxArea homeArea, GreyboxArea warningArea, GreyboxArea contactArea) {

    public boolean hasPursuitBehavior() {
        return true;
    }

    public boolean hasAttackBehavior() {
        return true;
    }

    public boolean hasWarningBehavior() {
        return true;
    }
}
