package com.kindred.engine.ui.widgets;

import com.kindred.engine.ui.Const;
import com.kindred.engine.ui.UIPanel;
import com.kindred.engine.ui.Vector2i;

/**
 * Placeholder panel for the minimap area within the sidebar.
 */
public class MinimapPanel extends UIPanel {

    public MinimapPanel(Vector2i position, Vector2i size) {
        super(position, size);
        setBackgroundColor(Const.COLOR_BG_MINIMAP); // Use constant for color
        // TODO: Add minimap rendering logic later
    }

    // Override render if specific minimap drawing is needed
    // @Override
    // public void render(Graphics g) {
    //     super.render(g); // Draw background
    //     // Add custom minimap drawing here
    // }
}
