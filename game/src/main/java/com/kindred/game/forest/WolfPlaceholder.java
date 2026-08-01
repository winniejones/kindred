package com.kindred.game.forest;

public record WolfPlaceholder(String id, GreyboxPoint spawnPosition, GreyboxArea homeArea) {

    public boolean hasPursuitBehavior() {
        return false;
    }

    public boolean hasAttackBehavior() {
        return false;
    }

    public boolean hasWarningBehavior() {
        return false;
    }
}
