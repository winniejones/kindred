package com.kindred.game.forest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ForestCrisisWolfEncounter {
    public static final int PURSUIT_STEP = 8;
    public static final float WOLF_ATTACK_RANGE = 40f;

    private final ForestCrisisGreybox greybox;
    private final ForestCrisisState crisis;
    private final List<WolfPlaceholder> wolves;
    private final Map<String, WolfRuntimeState> states = new LinkedHashMap<>();
    private final Set<String> defeatedWolves = new HashSet<>();

    private ForestCrisisWolfEncounter(ForestCrisisGreybox greybox, ForestCrisisState crisis) {
        this.greybox = greybox;
        this.crisis = crisis;
        this.wolves = List.copyOf(greybox.wolfPlaceholders());
        for (WolfPlaceholder wolf : wolves) {
            states.put(wolf.id(), new WolfRuntimeState(wolf.id(), wolf.spawnPosition(), WolfState.IDLE_AT_HOME));
        }
    }

    public static ForestCrisisWolfEncounter createDefault(ForestCrisisGreybox greybox, ForestCrisisState crisis) {
        return new ForestCrisisWolfEncounter(greybox, crisis);
    }

    public WolfEncounterUpdate update(GreyboxPoint playerPosition) {
        if (!greybox.isInsideThreatZone(playerPosition) || greybox.isInsideSafePlace(playerPosition)) {
            return breakContact();
        }

        WolfEncounterEvent event = WolfEncounterEvent.NONE;
        String eventWolfId = null;
        for (WolfPlaceholder wolf : wolves) {
            WolfRuntimeState state = states.get(wolf.id());
            if (state.state() == WolfState.DEFEATED) {
                continue;
            }
            if (state.state() == WolfState.PURSUING) {
                states.put(wolf.id(), new WolfRuntimeState(wolf.id(), pursuitStep(state.position(), playerPosition), WolfState.PURSUING));
                continue;
            }
            if (state.state() == WolfState.WARNING && wolf.contactArea().contains(playerPosition)) {
                states.put(wolf.id(), new WolfRuntimeState(wolf.id(), state.position(), WolfState.PURSUING));
                if (event == WolfEncounterEvent.NONE) {
                    event = WolfEncounterEvent.CONTACT_STARTED;
                    eventWolfId = wolf.id();
                }
                continue;
            }
            if (state.state() == WolfState.IDLE_AT_HOME && wolf.warningArea().contains(playerPosition)) {
                states.put(wolf.id(), new WolfRuntimeState(wolf.id(), state.position(), WolfState.WARNING));
                if (event == WolfEncounterEvent.NONE) {
                    event = WolfEncounterEvent.WARNING;
                    eventWolfId = wolf.id();
                }
            }
        }

        return snapshot(event, eventWolfId);
    }

    public boolean canAttackPlayer(String wolfId, GreyboxPoint playerPosition) {
        WolfRuntimeState state = states.get(wolfId);
        if (state == null || state.state() != WolfState.PURSUING) {
            return false;
        }
        int dx = playerPosition.x() - state.position().x();
        int dy = playerPosition.y() - state.position().y();
        return dx * dx + dy * dy <= WOLF_ATTACK_RANGE * WOLF_ATTACK_RANGE;
    }

    public WolfEncounterUpdate recordDefeat(String wolfId) {
        if (!states.containsKey(wolfId) || defeatedWolves.contains(wolfId)) {
            return snapshot(WolfEncounterEvent.NONE, wolfId);
        }
        defeatedWolves.add(wolfId);
        WolfRuntimeState state = states.get(wolfId);
        states.put(wolfId, new WolfRuntimeState(wolfId, state.position(), WolfState.DEFEATED));
        crisis.recordPredatorDefeat();
        return snapshot(WolfEncounterEvent.WOLF_DEFEATED, wolfId, "Forest Crisis Predator Defeat progress: " + crisis.predatorDefeats() + "/2 after " + wolfId);
    }

    public ForestCrisisGreybox greybox() {
        return greybox;
    }

    public List<WolfPlaceholder> wolves() {
        return wolves;
    }

    public WolfRuntimeState wolf(String wolfId) {
        return states.get(wolfId);
    }

    public List<WolfRuntimeState> wolfStates() {
        return List.copyOf(states.values());
    }

    private WolfEncounterUpdate breakContact() {
        boolean brokeContact = false;
        for (WolfPlaceholder wolf : wolves) {
            WolfRuntimeState state = states.get(wolf.id());
            if (state.state() == WolfState.PURSUING || state.state() == WolfState.WARNING) {
                states.put(wolf.id(), new WolfRuntimeState(wolf.id(), returnHome(state.position(), wolf), WolfState.RETURNING_HOME));
                brokeContact = true;
            } else if (state.state() == WolfState.RETURNING_HOME) {
                GreyboxPoint next = returnHome(state.position(), wolf);
                WolfState nextState = next.equals(wolf.spawnPosition()) ? WolfState.IDLE_AT_HOME : WolfState.RETURNING_HOME;
                states.put(wolf.id(), new WolfRuntimeState(wolf.id(), next, nextState));
            }
        }
        return snapshot(brokeContact ? WolfEncounterEvent.CONTACT_BROKEN : WolfEncounterEvent.NONE, null);
    }

    private GreyboxPoint pursuitStep(GreyboxPoint from, GreyboxPoint toward) {
        GreyboxPoint next = stepToward(from, toward, PURSUIT_STEP);
        if (greybox.isInsideSafePlace(next)) {
            return from;
        }
        return clampToArea(next, greybox.threatZone());
    }

    private GreyboxPoint returnHome(GreyboxPoint from, WolfPlaceholder wolf) {
        GreyboxPoint next = stepToward(from, wolf.spawnPosition(), PURSUIT_STEP);
        if (next.distanceSquaredTo(wolf.spawnPosition()) <= PURSUIT_STEP * PURSUIT_STEP) {
            next = wolf.spawnPosition();
        }
        return clampToArea(next, wolf.homeArea());
    }

    private GreyboxPoint stepToward(GreyboxPoint from, GreyboxPoint toward, int step) {
        int dx = toward.x() - from.x();
        int dy = toward.y() - from.y();
        double distance = Math.hypot(dx, dy);
        if (distance == 0 || distance <= step) {
            return toward;
        }
        return new GreyboxPoint(from.x() + (int) Math.round(dx / distance * step), from.y() + (int) Math.round(dy / distance * step));
    }

    private GreyboxPoint clampToArea(GreyboxPoint point, GreyboxArea area) {
        int x = Math.max(area.x(), Math.min(point.x(), area.x() + area.width() - 1));
        int y = Math.max(area.y(), Math.min(point.y(), area.y() + area.height() - 1));
        return new GreyboxPoint(x, y);
    }

    private WolfEncounterUpdate snapshot(WolfEncounterEvent event, String wolfId) {
        return WolfEncounterUpdate.of(event, wolfId, new ArrayList<>(states.values()));
    }

    private WolfEncounterUpdate snapshot(WolfEncounterEvent event, String wolfId, String developmentLog) {
        return WolfEncounterUpdate.of(event, wolfId, new ArrayList<>(states.values()), developmentLog);
    }
}
