package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

public class DeadComponent implements Component {
    /** The current visual stage of decay (0 = initial corpse, 1 = more decayed, etc.). */
    public int decayStage = 0;

    public DeadComponent() {}

    public DeadComponent(int decayStage) {
        this.decayStage = decayStage;
    }
}
