package com.kindred.game.forest;

import com.kindred.game.dialogue.ShepherdIntroductionDialogue;
import com.kindred.game.text.PlayerTextKey;

import java.util.List;
import java.util.Optional;

public class ForestCrisisGreybox {
    public static final String LEVEL_RESOURCE = "/assets/level/forest_crisis_greybox_map.png";
    public static final int TILE_SIZE = 16;
    public static final int INTERACTION_RANGE = 44;
    public static final int MIN_WARNING_MARGIN_TILES = 4;

    private final ForestCrisisState crisis;
    private final ShepherdIntroductionDialogue shepherdDialogue;
    private final GreyboxArea village = new GreyboxArea(64, 64, 176, 176);
    private final GreyboxArea shepherdsFarm = new GreyboxArea(384, 80, 240, 240);
    private final GreyboxArea threatZone = new GreyboxArea(320, 192, 560, 896);
    private final GreyboxArea safePlace = new GreyboxArea(384, 240, 128, 112);
    private final GreyboxPoint playerStart = new GreyboxPoint(120, 120);
    private final GreyboxPoint shepherdPosition = new GreyboxPoint(184, 136);
    private final GreyboxPoint farmApproach = new GreyboxPoint(432, 160);
    private final GreyboxPoint attackAftermathPosition = new GreyboxPoint(456, 152);
    private final GreyboxPoint predatorTrailPosition = new GreyboxPoint(500, 192);
    private final List<WolfPlaceholder> wolfPlaceholders = List.of(
            new WolfPlaceholder(
                    "wolf-1",
                    new GreyboxPoint(560, 496),
                    new GreyboxArea(512, 448, 128, 128),
                    new GreyboxArea(448, 384, 224, 224),
                    new GreyboxArea(528, 464, 64, 64)),
            new WolfPlaceholder(
                    "wolf-2",
                    new GreyboxPoint(728, 752),
                    new GreyboxArea(680, 704, 128, 128),
                    new GreyboxArea(608, 640, 240, 224),
                    new GreyboxArea(696, 720, 64, 64)),
            new WolfPlaceholder(
                    "wolf-3",
                    new GreyboxPoint(472, 944),
                    new GreyboxArea(424, 896, 128, 128),
                    new GreyboxArea(352, 832, 224, 224),
                    new GreyboxArea(440, 912, 64, 64)));
    private final List<GreyboxMarker> markers = List.of(
            new GreyboxMarker(shepherdPosition, 0xFFB8874A, true),
            new GreyboxMarker(attackAftermathPosition, 0xFF8B5A2B, false),
            new GreyboxMarker(predatorTrailPosition, 0xFF5B3A29, true),
            new GreyboxMarker(wolfPlaceholders.get(0).spawnPosition(), 0xFF777777, false),
            new GreyboxMarker(wolfPlaceholders.get(1).spawnPosition(), 0xFF777777, false),
            new GreyboxMarker(wolfPlaceholders.get(2).spawnPosition(), 0xFF777777, false));

    private ForestCrisisGreybox(ForestCrisisState crisis, ShepherdIntroductionDialogue shepherdDialogue) {
        this.crisis = crisis;
        this.shepherdDialogue = shepherdDialogue;
    }

    public static ForestCrisisGreybox createDefault(ForestCrisisState crisis) {
        return new ForestCrisisGreybox(crisis, ShepherdIntroductionDialogue.createDefault());
    }

    public GreyboxPoint playerStart() {
        return playerStart;
    }

    public GreyboxPoint shepherdPosition() {
        return shepherdPosition;
    }

    public GreyboxPoint shepherdsFarmApproach() {
        return farmApproach;
    }

    public GreyboxPoint attackAftermathPosition() {
        return attackAftermathPosition;
    }

    public GreyboxPoint predatorTrailPosition() {
        return predatorTrailPosition;
    }

    public GreyboxPoint threatZoneCenter() {
        return threatZone.center();
    }

    public GreyboxPoint safePlaceCenter() {
        return safePlace.center();
    }

    public GreyboxArea threatZone() {
        return threatZone;
    }

    public GreyboxArea safePlace() {
        return safePlace;
    }

    public GreyboxArea village() {
        return village;
    }

    public GreyboxArea shepherdsFarm() {
        return shepherdsFarm;
    }

    public List<WolfPlaceholder> wolfPlaceholders() {
        return wolfPlaceholders;
    }

    public List<GreyboxMarker> markers() {
        return markers;
    }

    public ForestCrisisPlace placeAt(GreyboxPoint point) {
        if (shepherdsFarm.contains(point)) {
            return ForestCrisisPlace.SHEPHERDS_FARM;
        }
        return ForestCrisisPlace.VILLAGE;
    }

    public boolean canWalkFromVillageToFarm() {
        return playerStart.x() < farmApproach.x() && playerStart.y() < farmApproach.y() + 96;
    }

    public Optional<IntroductionMoment> interactAt(GreyboxPoint playerPosition) {
        if (isWithinInteractionRange(playerPosition, shepherdPosition)) {
            return Optional.of(IntroductionMoment.of(shepherdDialogue.openingLine(), IntroductionMoment.Kind.DIALOGUE));
        }
        if (isWithinInteractionRange(playerPosition, predatorTrailPosition)) {
            crisis.discoverSign(EnvironmentalSign.PREDATOR_TRAIL);
            return Optional.of(IntroductionMoment.of(PlayerTextKey.OBSERVATION_PREDATOR_TRAIL_FIRST, IntroductionMoment.Kind.OBSERVATION_TEXT));
        }
        return Optional.empty();
    }

    public boolean isInsideThreatZone(GreyboxPoint point) {
        return threatZone.contains(point);
    }

    public boolean isInsideSafePlace(GreyboxPoint point) {
        return safePlace.contains(point);
    }

    private boolean isWithinInteractionRange(GreyboxPoint playerPosition, GreyboxPoint targetPosition) {
        return playerPosition.distanceSquaredTo(targetPosition) <= INTERACTION_RANGE * INTERACTION_RANGE;
    }
}
