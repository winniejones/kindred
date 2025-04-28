package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class InteractionEventComponent implements Component {
    public final int interactorId; // ID of the entity performing the interaction

    public InteractionEventComponent(int interactorId) {
        this.interactorId = interactorId;
    }
}
