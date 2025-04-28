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
    public Color backgroundColor;
    /** Text color . */
    public Color color;
    /** Reference to the parent panel (set via init). */
    protected UIPanel panel;
    /** Whether the component is currently active and should be updated/rendered. */
    public boolean active = true;

    /** Constructor setting only position. */
    public UIComponent(Vector2i position) {
        this.position = position;
        this.offset = new Vector2i(); // Initialize offset
        this.backgroundColor = Color.WHITE; // Default color
        this.color = Color.BLACK; // Default color
    }

    public UIComponent(int x, int y) {
        this.position = new Vector2i(x, y);
        this.offset = new Vector2i(); // Initialize offset
        this.backgroundColor = Color.WHITE; // Default color
        this.color = Color.BLACK; // Default color
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
     * Sets the active state of the component (fluent).
     * Inactive components are typically not updated or rendered.
     * @param active true if the component should be active, false otherwise.
     * @return This component instance for chaining.
     */
    public UIComponent setActive(boolean active) {
        this.active = active;
        return this;
    }

    /**
     * Sets the background color using an integer ARGB value.
     * @param backgroundColor ARGB color integer.
     * @return This component for chaining.
     */
    public UIComponent setBackgroundColor(int backgroundColor) {
        // Using Color(int rgb, boolean hasalpha) might be better if colors have alpha
        this.backgroundColor = new Color(backgroundColor, true); // Assume ARGB
        return this;
    }
    public UIComponent setBackgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }
    public UIComponent setColor(int color) {
        this.color = new Color(color, true);
        return this;
    }
    public UIComponent setColor(Color color) {
        this.color = color;
        return this;
    }

    public UIComponent setPosition(Vector2i position) {
        if (position != null) {
            this.position = position;
        }
        return this;
    }

    public UIComponent setPosition(int x, int y) {
        if (this.position == null) {
            this.position = new Vector2i(x, y);
        } else {
            this.position.set(x, y);
        }
        return this;
    }

    /**
     * Update logic for the component (e.g., checking input for buttons).
     * Called by parent panel or UIManager.
     * @param input The current state of input devices for this frame.
     * @param deltaTime Time elapsed since the last update in seconds.
     */
    // <<< Added update method accepting InputState >>>
    public void update(InputState input, float deltaTime) {
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

