package com.kindred.game.forest;

import com.kindred.engine.entity.components.ColliderComponent;
import com.kindred.engine.level.Level;
import com.kindred.engine.level.MapLoader;
import com.kindred.engine.level.SpawnPoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestCrisisGreyboxLevelTest {

    private final Level level = MapLoader.loadLevelFromImage(
            ForestCrisisGreybox.LEVEL_RESOURCE,
            ForestCrisisGreybox.TILE_SIZE);
    private final ForestCrisisGreybox greybox = ForestCrisisGreybox.createDefault(new ForestCrisisState());

    @Test
    void villageSpawnIsAuthoritativeAndPlayerColliderStartsOnWalkableTiles() {
        List<SpawnPoint> playerSpawns = level.getSpawnPoints().stream()
                .filter(spawn -> spawn.getType() == SpawnPoint.SpawnType.PLAYER)
                .toList();

        assertEquals(1, level.getSpawnPoints().size());
        assertEquals(1, playerSpawns.size());
        assertEquals(greybox.playerStart(), spawnCenter(playerSpawns.getFirst()));
        assertColliderWalkable(greybox.playerStart(), new ColliderComponent(15, 14, 8, 15));
    }

    @Test
    void shepherdFarmAftermathAndPredatorTrailAreReachableThroughWalkableTiles() {
        assertReachable(greybox.playerStart(), greybox.shepherdPosition());
        assertReachable(greybox.shepherdPosition(), greybox.shepherdsFarmApproach());
        assertReachable(greybox.shepherdsFarmApproach(), greybox.attackAftermathPosition());
        assertReachable(greybox.shepherdsFarmApproach(), greybox.predatorTrailPosition());
    }

    @Test
    void villageToFarmRouteIsNotBlocked() {
        assertReachable(greybox.playerStart(), greybox.shepherdsFarmApproach());
    }

    @Test
    void allGreyboxMarkersAreWithinLevelBoundsAndOnWalkableTiles() {
        for (GreyboxMarker marker : greybox.markers()) {
            assertInBounds(marker.position());
            assertFalse(level.isSolid(tileX(marker.position()), tileY(marker.position())));
        }
    }

    @Test
    void levelHasSolidOuterBoundaries() {
        for (int x = 0; x < level.getWidth(); x++) {
            assertTrue(level.isSolid(x, 0));
            assertTrue(level.isSolid(x, level.getHeight() - 1));
        }
        for (int y = 0; y < level.getHeight(); y++) {
            assertTrue(level.isSolid(0, y));
            assertTrue(level.isSolid(level.getWidth() - 1, y));
        }
    }

    private void assertColliderWalkable(GreyboxPoint position, ColliderComponent collider) {
        int left = position.x() + collider.offsetX;
        int top = position.y() + collider.offsetY;
        int right = left + collider.hitboxWidth - 1;
        int bottom = top + collider.hitboxHeight - 1;

        for (int y = top; y <= bottom; y += ForestCrisisGreybox.TILE_SIZE) {
            for (int x = left; x <= right; x += ForestCrisisGreybox.TILE_SIZE) {
                assertFalse(level.isSolid(x / ForestCrisisGreybox.TILE_SIZE, y / ForestCrisisGreybox.TILE_SIZE));
            }
        }
        assertFalse(level.isSolid(right / ForestCrisisGreybox.TILE_SIZE, bottom / ForestCrisisGreybox.TILE_SIZE));
    }

    private void assertReachable(GreyboxPoint start, GreyboxPoint goal) {
        assertTrue(isReachable(tileX(start), tileY(start), tileX(goal), tileY(goal)));
    }

    private boolean isReachable(int startX, int startY, int goalX, int goalY) {
        Queue<int[]> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new int[]{startX, startY});
        visited.add(startX + ":" + startY);

        while (!queue.isEmpty()) {
            int[] current = queue.remove();
            if (current[0] == goalX && current[1] == goalY) {
                return true;
            }

            addIfWalkable(queue, visited, current[0] + 1, current[1]);
            addIfWalkable(queue, visited, current[0] - 1, current[1]);
            addIfWalkable(queue, visited, current[0], current[1] + 1);
            addIfWalkable(queue, visited, current[0], current[1] - 1);
        }

        return false;
    }

    private void addIfWalkable(Queue<int[]> queue, Set<String> visited, int x, int y) {
        String key = x + ":" + y;
        if (visited.contains(key) || level.isSolid(x, y)) {
            return;
        }
        visited.add(key);
        queue.add(new int[]{x, y});
    }

    private GreyboxPoint spawnCenter(SpawnPoint spawn) {
        int tileSize = ForestCrisisGreybox.TILE_SIZE;
        return new GreyboxPoint(spawn.getTileX() * tileSize + tileSize / 2, spawn.getTileY() * tileSize + tileSize / 2);
    }

    private void assertInBounds(GreyboxPoint point) {
        assertTrue(point.x() >= 0 && point.x() < level.getWidth() * ForestCrisisGreybox.TILE_SIZE);
        assertTrue(point.y() >= 0 && point.y() < level.getHeight() * ForestCrisisGreybox.TILE_SIZE);
    }

    private int tileX(GreyboxPoint point) {
        return point.x() / ForestCrisisGreybox.TILE_SIZE;
    }

    private int tileY(GreyboxPoint point) {
        return point.y() / ForestCrisisGreybox.TILE_SIZE;
    }
}
