package com.kindred.game.forest;

import java.util.List;
import java.util.Optional;

public record WolfEncounterUpdate(WolfEncounterEvent event, String wolfId, List<WolfRuntimeState> wolves, String developmentLog) {

    public static WolfEncounterUpdate of(WolfEncounterEvent event, String wolfId, List<WolfRuntimeState> wolves) {
        return new WolfEncounterUpdate(event, wolfId, List.copyOf(wolves), null);
    }

    public static WolfEncounterUpdate of(WolfEncounterEvent event, String wolfId, List<WolfRuntimeState> wolves, String developmentLog) {
        return new WolfEncounterUpdate(event, wolfId, List.copyOf(wolves), developmentLog);
    }

    public Optional<String> developmentLogMessage() {
        return Optional.ofNullable(developmentLog);
    }
}
