package com.kindred.game.forest;

import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForestCrisisResourceInputTest {

    private final ForestCrisisResourceInput input = new ForestCrisisResourceInput();

    @Test
    void mapsBandageAndFoodKeysWhenAllowed() {
        assertEquals(ForestCrisisResourceAction.USE_BANDAGE, input.actionFor(KeyEvent.VK_B, false, false));
        assertEquals(ForestCrisisResourceAction.USE_FOOD, input.actionFor(KeyEvent.VK_F, false, false));
    }

    @Test
    void ignoresResourceKeysWhileChatFocusedOrPlayerDefeated() {
        assertEquals(ForestCrisisResourceAction.NONE, input.actionFor(KeyEvent.VK_B, true, false));
        assertEquals(ForestCrisisResourceAction.NONE, input.actionFor(KeyEvent.VK_F, true, false));
        assertEquals(ForestCrisisResourceAction.NONE, input.actionFor(KeyEvent.VK_B, false, true));
        assertEquals(ForestCrisisResourceAction.NONE, input.actionFor(KeyEvent.VK_F, false, true));
    }
}
