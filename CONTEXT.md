# Kindred

Kindred is a Nordic fantasy RPG context for describing the playable world, its progression, and the player's relationship to local communities and ecosystems.

## Language

**Vertical Slice**:
A short, locally playable segment that proves Kindred's core experience through movement, combat, NPC dialogue, mentor guidance, and a small persistent consequence of player choice.

**Player**:
The person-controlled character whose growth, choices, and understanding drive the local story. The player is locally rooted in the village but remains lightly defined so future character creation is not constrained.

**Player Address**:
NPCs address the player through tone and relational words such as neighbor, child, or one of the village, without using a fixed player name in the vertical slice.
_Avoid_: Fixed protagonist name

**Defeat**:
The player's temporary incapacitation at zero health, followed by recovery at a safe place without losing discovered signs, completed actions, XP, levels, or essential equipment.
_Avoid_: Permadeath, full reset

**Bandage**:
A consumable resource that restores a limited amount of health immediately.
_Avoid_: Potion economy

**Food**:
A consumable resource that creates a temporary recovery period where the player regains health gradually.
_Avoid_: Hunger system

**XP**:
Experience earned through both combat outcomes and understanding or resolving problems in the world.
_Avoid_: Combat-only progression

**Progression Parity**:
The principle that each legitimate crisis outcome grants enough total XP for the player to reach level 2, while differing in world state rather than character progression.
_Avoid_: Correct-path XP bonus

**Level**:
A progression state reached by accumulating XP.

**NPC**:
A non-player character who exists in the world as a social actor, not merely as a target or vendor.

**Elder**:
A mentor figure who helps the player notice relationships and causes through practical, tradition-bearing knowledge with a spiritual tone but no proven magic.
_Avoid_: Quest giver, tutorial narrator

**Interpretation Conversation**:
An elder conversation where the player's discovered signs are connected into possible relationships through guided questions.
_Avoid_: Exposition dump, solution reveal

**Reflection Conversation**:
An elder conversation after the crisis outcome where the player receives feedback on what changed and what consequences may follow.
_Avoid_: Score screen, morality judgment

**Intent-Based Free Text**:
A dialogue style where the player types what they want to say and the game maps the wording to a controlled dialogue intention and node.
_Avoid_: Secret keyword parser, fixed dialogue choices

**Dialogue Intention**:
The controlled meaning the game recognizes behind the player's free-text input, independent of exact phrasing, short answers, and reasonable spelling errors.
_Avoid_: Visible dialogue text as logic

**Intention Classifier**:
The replaceable mechanism that maps player free text to an allowed dialogue intention in the current conversation context.
_Avoid_: Dialogue logic owner

**Logger Foreperson**:
An NPC who represents the village's need for timber, work, and livelihood and can accept a limited forestry compromise.
_Avoid_: Villain, greedy logger

**Threatened Villager**:
An NPC who represents the immediate danger from predators near the village and wants safety restored quickly.
_Avoid_: Coward, simple victim

**Ecosystem Chain Effect**:
A visible problem whose immediate symptoms are caused by earlier disturbances in the local environment.
_Avoid_: Random encounter, monster problem

**Natural Cause**:
A crisis explanation grounded in human resource use and ecosystem response rather than a hidden supernatural force.
_Avoid_: Magical twist, evil corruption

**Root Cause**:
The underlying disturbance that explains an ecosystem chain effect and can be addressed for a more lasting change to the world.

**Local Crisis**:
A village-scale conflict where immediate danger, resource needs, and ecosystem disturbance collide.
_Avoid_: Monster quest

**Forest Crisis**:
The vertical slice's local crisis covering logging, forest disturbance, prey displacement, wolves moving toward the village, and the villagers' responses.
_Avoid_: Wolf quest

**Inciting Attack**:
The shepherd's report of a predator attack near the shepherd's farm that draws the player from village routine into the local crisis.
_Avoid_: Elder quest assignment

**Resource Conflict**:
A problem where opposing needs are legitimate, such as villagers needing timber while logging damages habitat.
_Avoid_: Good-versus-evil dilemma

**Protected Grove**:
A small forest area important to prey animals' food and shelter where logging can be temporarily paused as part of a local compromise.
_Avoid_: Sacred forest, no-logging zone

**Forestry Compromise**:
A practical agreement that protects the grove while allowing logging to continue elsewhere.
_Avoid_: Logging ban

**Habitat Restoration**:
Concrete work that makes the protected grove usable by prey animals again, such as repairing shelter, restoring food access, or clearing a safe passage.
_Avoid_: Nature magic, abstract healing

**Abandoned Grazing Site**:
An environmental sign showing that prey animals recently used an area but have now left it.

**Cleared Shelter**:
An environmental sign showing that logging removed cover or food that prey animals depended on.

**Predator Trail**:
An environmental sign showing that predators are moving from the forest edge toward village land.

**Wolf**:
A dangerous forest predator acting from hunger, territory, and disrupted habitat rather than malice.
_Avoid_: Evil monster

**Threat Zone**:
An area where a predator may detect, pursue, and attack the player until contact is broken.

**Safe Place**:
A location such as the village edge, shepherd's farm, or elder's area where predator contact can be broken.

**Breaking Contact**:
Ending a predator encounter by leaving the threat zone, maintaining distance briefly, or reaching a safe place.
_Avoid_: Exploit, failed combat

**Predator Defeat**:
An outcome where a predator no longer contributes to the immediate threat, whether by being killed or by a future non-lethal method such as being driven away.
_Avoid_: Kill-only outcome

**Symptom Response**:
An action that reduces the immediate threat without resolving the underlying disturbance.
_Avoid_: Wrong solution, bad path

**Persistent Consequence**:
A small world-state change that remains after the player's choice and shows that the choice mattered.
_Avoid_: Temporary feedback

**Crisis Outcome**:
The village area's resulting state after the player has handled the immediate danger through combat, root-cause resolution, or a combination of both.

**Combat Outcome**:
A crisis outcome where the player removes the immediate predator threat through combat while the root cause remains.

**Balance Outcome**:
A crisis outcome where the player reduces the conflict between forestry, prey animals, and predators by addressing the ecosystem chain effect.

**Balance Path**:
The main path where the player understands and addresses the underlying imbalance between village, forestry, prey animals, and predators without requiring any predator defeat.
_Avoid_: Pacifist path

**Forest Route**:
The risky route connecting the village, damaged grove, and alternative logging area where the player can use observation, timing, positioning, and retreat to avoid combat.
_Avoid_: Safe peaceful corridor

**Observation Point**:
A relatively safe place where the player can read predator movement before choosing a route through or around a threat zone.

**Observation Text**:
A brief, low-key line shown when the player examines an environmental sign, without creating an automatic journal entry, checklist, or objective marker.
_Avoid_: Quest popup, clue checklist

**Diegetic Gate**:
A progression requirement communicated through believable world dialogue or circumstances rather than explicit counters, checklists, or system labels.
_Avoid_: Hidden arbitrary lock

**Mixed Outcome**:
A crisis outcome where the player combines combat with partial root-cause resolution, making the village safer while leaving recovery incomplete.

**First Improvement**:
The player's first small level-up choice, earned at level 2 after resolving the vertical slice's local crisis. All first improvements remain available regardless of crisis outcome.
_Avoid_: Full build, class choice

**Vitality Improvement**:
A first improvement that increases the player's maximum health.

**Melee Improvement**:
A first improvement that slightly increases the player's melee damage.

**Observation Improvement**:
A first improvement that makes environmental signs easier to notice and can reveal extra observations or dialogue options.
_Avoid_: Non-combat dump stat

## Places

**Village**:
The local community threatened by predators and dependent on nearby timber.

**Shepherd's Farm**:
The place representing the immediate threat to people and domestic animals near the village.

**Damaged Grove**:
The logged grove whose lost shelter and food pushed prey animals away.

**Forest Edge**:
The boundary area where predator signs show movement toward village land.

**Observation Cliff**:
A safe vantage point for reading predator movement along the forest route.

**Alternative Logging Area**:
The place where forestry can continue with less impact after the forestry compromise.
