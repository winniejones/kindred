package com.kindred.game.forest;

import java.awt.event.KeyEvent;

public class ForestCrisisResourceInput {

    public ForestCrisisResourceAction actionFor(int keyCode, boolean chatFocused, boolean playerDefeated) {
        if (chatFocused || playerDefeated) {
            return ForestCrisisResourceAction.NONE;
        }
        if (keyCode == KeyEvent.VK_B) {
            return ForestCrisisResourceAction.USE_BANDAGE;
        }
        if (keyCode == KeyEvent.VK_F) {
            return ForestCrisisResourceAction.USE_FOOD;
        }
        return ForestCrisisResourceAction.NONE;
    }
}
