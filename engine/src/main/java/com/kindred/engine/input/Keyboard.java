package com.kindred.engine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

    // Increase size slightly if using more keys, 120 is usually enough for common keys
    private static final int KEY_COUNT = 120;
    private final boolean[] currentKeys = new boolean[KEY_COUNT];
    private final boolean[] lastKeys = new boolean[KEY_COUNT]; // <<< Store previous frame's state

    // Public fields for easy access to movement/common keys (updated each frame)
    public boolean up, down, left, right, space, interact; // <<< Added interact (E key)

    /**
     * Updates the state of the public boolean fields (up, down, etc.)
     * and copies the current key states to the lastKeys array for the next frame's check.
     * This should be called ONCE per game loop update cycle, BEFORE processing input.
     */
    public void update() {
        // Copy current state to last state BEFORE updating public fields
        System.arraycopy(currentKeys, 0, lastKeys, 0, KEY_COUNT);

        // Update public boolean fields based on CURRENT state
        up = currentKeys[KeyEvent.VK_UP] || currentKeys[KeyEvent.VK_W];
        down = currentKeys[KeyEvent.VK_DOWN] || currentKeys[KeyEvent.VK_S];
        left = currentKeys[KeyEvent.VK_LEFT] || currentKeys[KeyEvent.VK_A];
        right = currentKeys[KeyEvent.VK_RIGHT] || currentKeys[KeyEvent.VK_D];
        space = currentKeys[KeyEvent.VK_SPACE];
        interact = currentKeys[KeyEvent.VK_E]; // <<< Check E key for interact
    }

    /**
     * Checks if a specific key is currently held down.
     * @param keyCode The KeyEvent key code (e.g., KeyEvent.VK_SPACE).
     * @return true if the key is currently pressed, false otherwise.
     */
    public boolean isKeyDown(int keyCode) {
        if (keyCode >= 0 && keyCode < KEY_COUNT) {
            return currentKeys[keyCode];
        }
        return false; // Key code out of bounds
    }

    /**
     * Checks if a specific key was just pressed down in this frame
     * (i.e., it's down now but was up in the previous frame).
     * Requires update() to be called each frame.
     *
     * @param keyCode The KeyEvent key code (e.g., KeyEvent.VK_E).
     * @return true if the key was just pressed this frame, false otherwise.
     */
    public boolean isKeyPressed(int keyCode) {
        if (keyCode >= 0 && keyCode < KEY_COUNT) {
            // Key is down now AND was not down last frame
            return currentKeys[keyCode] && !lastKeys[keyCode];
        }
        return false; // Key code out of bounds
    }


    // --- KeyListener Methods ---

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < KEY_COUNT) {
            currentKeys[keyCode] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < KEY_COUNT) {
            currentKeys[keyCode] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not typically used for game input polling
    }
}
