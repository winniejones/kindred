package com.kindred.engine.ui.listener;

import com.kindred.engine.ui.UIButton;

import java.awt.*;

/**
 * Listener class for button state changes (hover, press, release).
 * Provides default visual feedback by changing button color.
 * Can be extended for custom feedback.
 */
public class UIButtonListener {

    /** Called when the mouse cursor enters the button's bounds. */
    public void entered(UIButton button) {
        //button.setColor(new Color(0xcdcdcd)); // Lighter gray
    }

    /** Called when the mouse cursor exits the button's bounds. */
    public void exited(UIButton button) {
        //button.setColor(new Color(0xaaaaaa)); // Default gray
    }

    /** Called when the mouse button is pressed down while over the button. */
    public void pressed(UIButton button) {
        //button.setColor(new Color(0xcc2222)); // Reddish
    }

    /** Called when the mouse button is released while over the button (triggers action). */
    public void released(UIButton button) {
        // Restore hover color immediately after release triggers action
        //button.setColor(new Color(0xcdcdcd));
    }
}