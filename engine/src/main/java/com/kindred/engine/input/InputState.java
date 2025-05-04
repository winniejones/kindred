package com.kindred.engine.input;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the state of input devices (primarily mouse for UI) for a single frame/update cycle.
 */
public class InputState {
    public static final int MOUSE_LEFT = java.awt.event.MouseEvent.BUTTON1;

    // Mouse Position
    public int mouseX = -1;
    public int mouseY = -1;

    // Mouse Button State (Current frame)
    private Set<Integer> buttonsDown = new HashSet<>();

    // Mouse Button Events (Occurred *since* last update)
    private Set<Integer> buttonsPressedThisFrame = new HashSet<>();
    private Set<Integer> buttonsReleasedThisFrame = new HashSet<>();

    // --- Methods to Update State (Called by Listeners) ---

    public void updateMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public void setButtonDown(int buttonCode) {
        if (buttonsDown.add(buttonCode)) { // Add returns true if element was not already present
            buttonsPressedThisFrame.add(buttonCode); // Mark as pressed this frame
            // System.out.println("Pressed: " + buttonCode); // Debug
        }
    }

    public void setButtonUp(int buttonCode) {
        if (buttonsDown.remove(buttonCode)) { // Remove returns true if element was present
            buttonsReleasedThisFrame.add(buttonCode); // Mark as released this frame
            // System.out.println("Released: " + buttonCode); // Debug
        }
    }

    /**
     * Clears the "pressed/released this frame" flags.
     * Should be called at the END of the update cycle after all systems have processed input.
     */
    public void clearFrameEvents() {
        buttonsPressedThisFrame.clear();
        buttonsReleasedThisFrame.clear();
    }

    // --- Methods to Query State (Called by Systems/UI) ---

    /** Checks if a specific mouse button is currently held down. */
    public boolean isButtonDown(int buttonCode) {
        return buttonsDown.contains(buttonCode);
    }

    /** Checks if a specific mouse button was just pressed down in this frame. */
    public boolean isButtonPressed(int buttonCode) {
        return buttonsPressedThisFrame.contains(buttonCode);
    }

    /** Checks if a specific mouse button was just released in this frame. */
    public boolean isButtonReleased(int buttonCode) {
        return buttonsReleasedThisFrame.contains(buttonCode);
    }

    /** Gets the current mouse position. */
    public Point getMousePosition() {
        return new Point(mouseX, mouseY);
    }
}

