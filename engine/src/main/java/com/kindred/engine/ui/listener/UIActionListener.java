package com.kindred.engine.ui.listener;

/**
 * Interface for handling button click actions.
 */
@FunctionalInterface // Good practice for single-method interfaces
public interface UIActionListener {
    /** Method called when the associated UI element's action is performed (e.g., button clicked). */
    void perform();
}
