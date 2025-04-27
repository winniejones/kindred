package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

/**
 * Component storing a display name for an entity.
 */
public class NameComponent implements Component {
    public String name;

    public NameComponent(String name) {
        this.name = (name != null && !name.isBlank()) ? name : "Unnamed";
    }
}
