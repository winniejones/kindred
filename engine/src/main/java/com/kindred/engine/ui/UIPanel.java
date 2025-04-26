package com.kindred.engine.ui;

import com.kindred.engine.input.InputState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A UI component that acts as a container for other UI components.
 * Manages layout (via offsets) and rendering of its children.
 */
@Slf4j
public class UIPanel extends UIComponent {

    private List<UIComponent> components = new ArrayList<>();
    /** Size of the panel. */
    public Vector2i size;

    /**
     * Creates a new UIPanel.
     * @param position Position relative to its parent (or screen).
     * @param size Dimensions of the panel.
     */
    public UIPanel(Vector2i position, Vector2i size) {
        super(position); // Call UIComponent constructor
        this.size = size;
        // Default panel background color (semi-transparent gray example)
        // Use Color(r, g, b, a) for transparency
        color = new Color(100, 100, 100, 180); // RGBA example
    }

    /**
     * Adds a child component to this panel.
     * Initializes the child and sets its parent reference.
     * @param component The UIComponent to add.
     */
    public void addComponent(UIComponent component) {
        if (component != null) {
            component.init(this); // Let component know its parent
            components.add(component);
        }
    }

    /** Updates the panel and all its active child components. */
    @Override
    public void update(InputState input) {
        if (!active) return;

        Vector2i absolutePosition = getAbsolutePosition();
        log.trace("Panel update [{}@{}]: Calculated AbsolutePos = {}", this.getClass().getSimpleName(), Integer.toHexString(hashCode()), absolutePosition);

        // Iterate in reverse if needed for input processing order (top elements first)
        for (int i = components.size() - 1; i >= 0; i--) {
            UIComponent component = components.get(i);
            if (component.active) {
                component.setOffset(absolutePosition);
                // <<< Pass input state down to child component's update >>>
                log.trace("Panel update [{}@{}]: Setting offset for child {} to {}", this.getClass().getSimpleName(), Integer.toHexString(hashCode()), component.getClass().getSimpleName(), absolutePosition);
                component.update(input);
            }
        }
    }

    /**
     * Updates the panel and all its active child components.
     * Sets the correct offset for children before updating them.
     */
    @Override
    public void update() {
        // This version is called if input state isn't passed down (e.g. from base class loop)
        // It won't work correctly for buttons needing input.
        // Ideally, the base UIComponent update signature should also take InputState.
        update(new InputState()); // Pass a dummy state? Not ideal.
        // Better: Ensure update(InputState) is always called.
    }

    /**
     * Renders the panel background and all its active child components.
     * @param g The Graphics context to draw on.
     */
    @Override
    public void render(Graphics g) {
        if (!active) return; // Skip if panel is inactive

        // Calculate absolute position for rendering
        Vector2i absolutePosition = getAbsolutePosition();

        // Draw panel background
        g.setColor(this.color); // Use the panel's color
        g.fillRect(absolutePosition.x, absolutePosition.y, size.x, size.y);

        // Render child components (they will use their own absolute position)
        for (UIComponent component : components) {
            if (component.active) {
                component.render(g);
            }
        }
    }
}
