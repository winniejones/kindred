package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;

import java.awt.*;

/**
 * Base class for all UI elements.
 * Holds position and provides basic update/render methods.
 */
public class UIComponent {

    /** Position relative to the parent container (or screen if top-level). */
    public Vector2i position;
    /** The calculated offset from the screen origin (set by parent UIPanel). */
    protected Vector2i offset;
    /** Background color (optional, specific components might handle color differently). */
    public Color color;
    /** Reference to the parent panel (set via init). */
    protected UIPanel panel;
    /** Whether the component is currently active and should be updated/rendered. */
    public boolean active = true;

    /** Constructor setting only position. */
    public UIComponent(Vector2i position) {
        this.position = position;
        this.offset = new Vector2i(); // Initialize offset
        this.color = Color.WHITE; // Default color
    }

    // Optional constructor if size is known at base level
    // public UIComponent(Vector2i position, Vector2i size) {
    //     this.position = position;
    //     // this.size = size; // Size might be specific to subclasses like UIPanel/UIButton
    //     this.offset = new Vector2i();
    //     this.color = Color.WHITE;
    // }

    /**
     * Initializes the component, primarily setting its parent panel.
     * Called by UIPanel when a component is added.
     * @param panel The parent UIPanel.
     */
    void init(UIPanel panel) {
        this.panel = panel;
    }

    /**
     * Sets the background color using an integer ARGB value.
     * @param color ARGB color integer.
     * @return This component for chaining.
     */
    public UIComponent setColor(int color) {
        // Using Color(int rgb, boolean hasalpha) might be better if colors have alpha
        this.color = new Color(color, true); // Assume ARGB
        return this;
    }

    /**
     * Sets the background color using a Color object.
     * @param color The Color object.
     * @return This component for chaining.
     */
    public UIComponent setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * Update logic for the component (e.g., checking input for buttons).
     * Called by parent panel or UIManager.
     * @param input The current state of input devices for this frame.
     */
    // <<< Added update method accepting InputState >>>
    public void update(InputState input) {
        // Base implementation does nothing. Subclasses override this.
    }

    /**
     * Deprecated or fallback update method without input state.
     * Ideally, systems should always call update(InputState).
     */
    // <<< Keep the old update() or remove it? Keep for now, maybe mark deprecated >>>
    @Deprecated
    public void update() {
        // Call the new update with a dummy state if absolutely necessary,
        // but log a warning as input-dependent components won't work.
        // System.err.println("Warning: UIComponent update() called without InputState for " + this.getClass().getSimpleName());
        // update(new InputState()); // Avoid creating new objects frequently
    }

    /**
     * Render logic for the component.
     * Called by parent panel or UIManager.
     * @param g The Graphics context to draw on.
     */
    public void render(Graphics g) {
        // Base implementation does nothing
    }

    /**
     * Gets the absolute screen position of the component (position + offset).
     * @return A new Vector2i representing the absolute position.
     */
    public Vector2i getAbsolutePosition() {
        // Return a new vector to avoid modifying internal state
        return position.added(offset);
    }

    /**
     * Sets the offset, usually called by the parent UIPanel.
     * @param offset The offset from the screen origin to the parent panel's origin.
     */
    void setOffset(Vector2i offset) {
        this.offset = offset;
    }
}

