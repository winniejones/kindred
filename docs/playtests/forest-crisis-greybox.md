# Forest Crisis Greybox Manual Playtest

## Launch

Run from the repository root:

```bash
./gradlew :game:run
```

## Controls

- Move with `WASD` or arrow keys.
- Interact with `E`.
- Attack remains on `Space`, but Forest Crisis wolf behavior is not part of this greybox prerequisite.
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
10. The red outlined rectangle is the placeholder Threat Zone.
11. The green outlined rectangle is the placeholder Safe Place.
12. Three grey placeholder wolf markers exist inside the Threat Zone. They are spawn/home placeholders only; they should not pursue, warn, attack, or break contact until issue #8.

## Out Of Scope For This Playtest

- Wolf pursuit, warning, attack, and breaking contact.
- Healing, food, defeat recovery, XP, level-up, Elder dialogue, Logger dialogue, Balance actions, or crisis completion.
- Journal, checklist, counter, objective marker, or production artwork.
