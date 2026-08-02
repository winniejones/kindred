# Forest Crisis Greybox Manual Playtest

## Launch

Run from the repository root:

```bash
./gradlew :game:run
```

The runtime loads `/assets/level/forest_crisis_greybox_map.png` as the authoritative level for this path, so the visible greybox and tile collision should agree. The current greybox map is `60 x 96` tiles.

## Controls

- Move with `WASD` or arrow keys.
- Interact with `E`.
- Attack remains on `Space`.
- Use a Bandage with `B`.
- Use Food with `F`.
- Toggle chat input with `Ctrl+Enter`.

## Expected Path

1. The Player starts in the Village greybox area near the Shepherd marker.
2. The chat shows a quiet Village moment and a diegetic `Press E` hint.
3. Press `E` away from the Shepherd marker. Nothing in the Forest Crisis introduction should advance.
4. Walk to the Shepherd marker and press `E` within range. The Shepherd reports the Inciting Attack and points toward Shepherd's Farm.
5. Walk right/down toward Shepherd's Farm.
6. Confirm the visible attack aftermath marker near the farm.
7. Confirm the Predator Trail marker near the farm/forest edge.
8. Press `E` away from the Predator Trail marker. The trail should not be examined.
9. Walk to the Predator Trail marker and press `E` within range. Low-key Observation Text appears.
10. The large red outlined rectangle is the placeholder Threat Zone.
11. The green outlined rectangle is the placeholder Safe Place. It is inside the red Threat Zone so Safe Place escape can be tested separately from leaving the Threat Zone.
12. Three grey wolf markers exist inside the Threat Zone, separated enough that normal movement should activate one wolf at a time.
13. Yellow rectangles are temporary wolf warning areas. Orange rectangles are temporary hostile-contact areas. Each orange area sits inside its yellow area with at least four tiles of warning-only margin.
14. Approach each wolf through its yellow rectangle without stepping into orange. A warning line should appear, and that wolf should not pursue or attack yet.
15. Walk around in the same yellow-only space. The wolf should remain warned without immediately starting hostile contact.
16. Step into that wolf's orange rectangle. A hostile-contact line should appear, and that wolf should begin pursuing within its authored home area.
17. Fight or turn inside the enlarged Threat Zone. There should be room to reposition before choosing an escape route.
18. Leave the Threat Zone. Contact should break and the wolf should fall back toward home instead of following indefinitely.
19. Re-enter hostile contact, then retreat into the green Safe Place while still inside the red Threat Zone. Contact should break because of Safe Place entry, and the wolf should not enter the Safe Place.
20. Repeat warning/contact/return checks for all three wolves. Each wolf should activate from its own yellow/orange area.
21. Defeat wolves with `Space`. The terminal prints `Forest Crisis Predator Defeat progress: N/2 after wolf-X` via stdout and the logger. Two relevant wolf defeats make the combat outcome ready internally, but no final outcome or slice completion should trigger in this increment.

## Recovery And Resources

1. On startup, confirm the chat reports `3 bandages and 2 food`.
2. Let a Wolf damage the Player and confirm the HP panel drops.
3. Press `B`. HP should immediately rise by 35, clamped to maximum health, and one Bandage is consumed internally.
4. Press `F`. Food recovery should start and HP should rise gradually over about 10 seconds for 30 total healing.
5. Press `F` again while Food recovery is active. The game should report that Food recovery is already active and should not consume another Food.
6. Let a Wolf reduce the Player to zero health.
7. Confirm the same Player recovers immediately at the green Safe Place with 50% of maximum health.
8. Confirm Defeat cost feedback: one Bandage is removed if available; otherwise one Food is removed; otherwise no resource loss occurs.
9. Confirm Wolf contact stops after recovery at the Safe Place.
10. Confirm movement, collision, interaction, and attacks still work after recovery.
11. Confirm Shepherd and Predator Trail interactions still work and their state persists after Defeat.
12. Confirm Predator Defeat progress and other Forest Crisis state persist after Defeat.
13. Stand at the green Safe Place and press `E` to claim the emergency cache. It should grant exactly 1 Bandage and 1 Food.
14. Press `E` at the cache again. It should report that the cache is empty.
15. Confirm Defeat recovery does not automatically claim the emergency cache.
16. Confirm no inventory HUD, hunger system, shop, crafting, or permanent resource counter was introduced.

## Out Of Scope For This Playtest

- XP, level-up, Elder dialogue, Logger dialogue, Balance actions, or crisis completion.
- Journal, checklist, counter, objective marker, or production artwork.
