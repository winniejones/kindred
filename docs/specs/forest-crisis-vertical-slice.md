# Spec: Forest Crisis Vertical Slice

## Problem Statement

Kindred needs a first playable vertical slice that proves the core RPG experience in a small, local, 10-15 minute singleplayer scenario. The slice must demonstrate movement, combat, NPC dialogue, an Elder mentor interaction, XP-based level progression, and a small Persistent Consequence of player choice.

The current codebase already has early ECS support for player movement, collision, combat, NPC interaction markers, health, XP, level-up events, UI, and a manually wired `game` application. It does not yet have a complete authored crisis, controlled dialogue model, localizable player-facing text, Forest Crisis world state, or tests. The feature must therefore define a high-level scenario seam above the existing ECS systems rather than scattering untestable behavior across low-level components.

The player-facing problem is that early Kindred must not teach players that violence is the only real path to XP and progression. Forest Crisis must prove that combat, observation, dialogue, escape, and understanding an Ecosystem Chain Effect can all be legitimate parts of progression, while still keeping danger real and the scope small.

## Solution

Build the Forest Crisis as Kindred's first Vertical Slice. The Player starts in the Village, sees the Inciting Attack when the Threatened Villager reports a wolf attack near the Shepherd's Farm, investigates the Forest Edge and Damaged Grove, and chooses how to make the area safe enough for the immediate crisis to end.

The Local Crisis is caused by a Natural Cause, not a Magical Twist: logging near the Village damages prey animals' food and shelter, prey animals leave parts of the forest, and hungry Wolves move closer to the Shepherd's Farm and Village. This is a Resource Conflict, not a good-versus-evil dilemma. The Logger Foreperson represents timber, work, and livelihood. The Threatened Villager represents immediate safety. The Elder helps the Player interpret discovered signs and reflect on consequences without becoming a Quest Giver or solution reveal.

The slice supports exactly three legitimate Crisis Outcomes:

- Combat Outcome: at least two of three Wolves are defeated and zero Balance actions are completed. The Village is safer for now, but the Root Cause remains.
- Balance Outcome: both Balance actions are completed. The Player establishes a Forestry Compromise and performs Habitat Restoration, causing prey animals to begin returning and wolf pressure near the Village to decrease. This outcome must be reachable without any Predator Defeat.
- Mixed Outcome: at least one Wolf is defeated and exactly one Balance action is completed. The Village is safer and the Root Cause is partly addressed, but recovery is incomplete.

All three legitimate outcomes lead to Level 2 through Progression Parity. Partial XP can be awarded for relevant actions, but the final crisis reward must fill the remaining XP so each legitimate outcome reaches Level 2 without giving a significant progression advantage to completionist maximization. The Player then chooses one First Improvement: Vitality Improvement, Melee Improvement, or Observation Improvement. All three remain available regardless of Crisis Outcome.

Dialogue uses Intent-Based Free Text as a core Kindred principle, implemented in tightly limited scope for the slice. The Player types what they want to say, and a deterministic Intention Classifier maps the text to controlled Dialogue Intentions allowed by the current conversation context and world state. The slice only needs full Intent-Based Free Text for the Threatened Villager, Elder, and Logger Foreperson. Dialogue and other localizable player-facing text must be stored in language resources and retrieved through stable text keys. Visible text must never be used as a logic identifier.

## User Stories

1. As a player, I want to start in the Village, so that the crisis affects a place that feels like my home.
2. As a player, I want a short moment of normal village movement before the crisis begins, so that I can learn basic controls before danger appears.
3. As a player, I want the Threatened Villager to report an attack near the Shepherd's Farm, so that I understand the immediate danger.
4. As a player, I want the Elder to react without assigning me a quest, so that the crisis feels grounded in the world rather than delivered by a quest giver.
5. As a player, I want to follow the route from the Village to the Shepherd's Farm, so that I naturally reach the first investigation site.
6. As a player, I want to talk to the Threatened Villager at the Shepherd's Farm, so that I understand how the Wolves are affecting people and domestic animals.
7. As a player, I want to see visible damage at the Shepherd's Farm, so that the attack is communicated through the world and not only dialogue.
8. As a player, I want to examine Predator Trail signs near the Shepherd's Farm, so that I can begin understanding where the Wolves are moving.
9. As a player, I want low-key Observation Text when I examine a sign, so that the game clarifies what I noticed without opening a quest checklist.
10. As a player, I want no automatic journal of discovered signs, so that I must observe, remember, and reason about the world.
11. As a player, I want examined signs to remain in the world when possible, so that I can return and reconsider them.
12. As a player, I want the first Wolf encounter to begin with sound, movement, and warning behavior, so that I can read danger before being attacked.
13. As a player, I want Wolves to be dangerous animals rather than evil monsters, so that the conflict remains morally nuanced.
14. As a player, I want the first Wolf to warn before pursuing, so that I can choose whether to retreat, avoid, or fight.
15. As a player, I want Threat Zones to be readable through environmental signals, so that I do not need heavy UI markers to understand danger.
16. As a player, I want denser tracks, warning sounds, damaged fences, dead small animals, and disturbed vegetation to indicate Threat Zones, so that the world teaches me where danger is.
17. As a player, I want to break contact with a Wolf by reaching a Safe Place or creating enough distance, so that retreat is a legitimate skill.
18. As a player, I want Wolves to return gradually to their area after contact is broken, so that I cannot exploit them by dragging them indefinitely.
19. As a player, I want to fight Wolves if I choose, so that combat is a valid way to protect the Village.
20. As a player, I want defeating one Wolf to give combat XP but not end the crisis, so that symptom response is useful but incomplete.
21. As a player, I want the Combat Outcome to require at least two of three Predator Defeats and zero Balance actions, so that the immediate danger is reduced without requiring extermination or partial Root Cause work.
22. As a player, I want defeating all three Wolves to remain a Combat Outcome rather than a separate branch, so that the slice stays focused.
23. As a player, I want the Balance Path to be possible without defeating any enemy, so that Kindred proves progression without required violence.
24. As a player, I want the Balance Path to remain risky, so that avoiding violence still requires skill and attention.
25. As a player, I want the Forest Route to connect the Village, Damaged Grove, Observation Cliff, and Alternative Logging Area, so that observation and movement are meaningful parts of the crisis.
26. As a player, I want to use terrain, vegetation, rocks, timing, and retreat along the Forest Route, so that avoiding Wolves feels active rather than passive.
27. As a player, I want a short dangerous passage and a longer safer route, so that I can choose risk based on my confidence.
28. As a player, I want the Observation Cliff to let me read Wolf movement, so that I can plan a route through or around a Threat Zone.
29. As a player, I want to find an Abandoned Grazing Site, so that I can see prey animals recently left the area.
30. As a player, I want to find Cleared Shelter in the Damaged Grove, so that I can see how logging removed food or cover.
31. As a player, I want to find a Predator Trail toward the Village, so that I can connect the Wolves' movement to the crisis.
32. As a player, I want all three main environmental signs to be required for the full Balance Path, so that persuasion depends on evidence rather than guessing.
33. As a player, I want each required sign to be revisitable, so that missing it the first time does not permanently block me.
34. As a player, I want each required sign to have both environmental signals and NPC or Elder hints, so that no-journal play remains fair.
35. As a player, I want to return to the Elder with partial discoveries, so that I can receive interpretation without being given the answer.
36. As a player, I want the Elder's Interpretation Conversation to depend on what I have actually found, so that the conversation respects my discoveries.
37. As a player, I want the Elder to ask guiding questions, so that I can infer the Ecosystem Chain Effect myself.
38. As a player, I want the Elder not to provide missing evidence, so that investigation remains meaningful.
39. As a player, I want to approach the Logger Foreperson before I have evidence, so that the world does not artificially block conversation.
40. As a player, I want the Logger Foreperson to reject unsupported accusations diegetically, so that I understand I need stronger evidence without seeing counters.
41. As a player, I want the Logger Foreperson to ask what I have actually seen, so that the Diegetic Gate points me back toward investigation.
42. As a player, I want to be unable to convince the Logger Foreperson before finding all three main signs and interpreting them with the Elder, so that the Forestry Compromise is earned by understanding.
43. As a player, I want prior failed persuasion to be recoverable, so that experimentation does not permanently lock the Balance Path.
44. As a player, I want rude or forceful earlier dialogue to affect tone but not permanently block compromise, so that the world remembers without soft-locking me.
45. As a player, I want the Logger Foreperson to accept the compromise reluctantly and practically, so that the Resource Conflict remains credible.
46. As a player, I want the Logger Foreperson to acknowledge the cost of moving work, so that the compromise is not treated as free or obvious.
47. As a player, I want the Forestry Compromise to protect the Damaged Grove while allowing work elsewhere, so that logging is not framed as purely evil.
48. As a player, I want to mark the Alternative Logging Area, so that the Logger Foreperson has a practical path forward.
49. As a player, I want to restore the Damaged Grove through simple interactions, so that the Balance Path has concrete action beyond dialogue.
50. As a player, I want Habitat Restoration to include repairing shelter, food access, or passage, so that prey animals have a reason to return.
51. As a player, I want exactly two Balance actions to determine the full Balance Outcome, so that the route is clear and small enough for a 10-15 minute slice.
52. As a player, I want completing only one Balance action plus defeating a Wolf to produce the Mixed Outcome, so that partial Root Cause work matters.
53. As a player, I want completing both Balance actions to produce the Balance Outcome even if I fought earlier, so that full Root Cause resolution takes priority over route purity.
54. As a player, I want one Balance action without Predator Defeat to leave the crisis incomplete, so that the immediate danger still matters.
55. As a player, I want the crisis to become ready to conclude in the field but not formally end there, so that I have a reason to return to the Elder.
56. As a player, I want the formal ending to occur through the Elder's Reflection Conversation, so that consequences and progression are framed through the mentor relationship.
57. As a player, I want the world state to change before the Elder's final reflection, so that I can see what my actions caused before hearing about them.
58. As a player, I want the Elder's Reflection Conversation to differ by Combat Outcome, Balance Outcome, and Mixed Outcome, so that my path is acknowledged.
59. As a player, I want the Elder to avoid moral judgment, so that no legitimate outcome is treated as the only correct path.
60. As a player, I want the Combat Outcome reflection to acknowledge courage and safety while noting unresolved imbalance, so that symptom response is respected but not overstated.
61. As a player, I want the Balance Outcome reflection to acknowledge understanding and compromise while noting recovery takes time, so that the outcome is not framed as instant perfection.
62. As a player, I want the Mixed Outcome reflection to acknowledge both protection and partial restoration, so that incomplete recovery is clear.
63. As a player, I want the Threatened Villager's final reaction to differ by outcome, so that immediate safety has a human face.
64. As a player, I want the Logger Foreperson's final reaction to differ by outcome, so that forestry consequences are visible socially.
65. As a player, I want the Combat Outcome world state to show fewer Wolves near the Village but continued damage in the forest, so that the Root Cause remains visible.
66. As a player, I want the Balance Outcome world state to show returning prey signs and shifted logging work, so that Root Cause work is visible.
67. As a player, I want the Mixed Outcome world state to show improvement plus unresolved signs, so that partial resolution is visible.
68. As a player, I want each outcome to show changes at the Damaged Grove or grazing area, Wolf presence or signs, and NPC reactions, so that consequences appear across the world.
69. As a player, I want all legitimate outcomes to grant enough total XP to reach Level 2, so that progression does not privilege one moral route.
70. As a player, I want partial XP for relevant actions, so that investigation, combat, interpretation, persuasion, and restoration all feel recognized.
71. As a player, I want the final crisis reward to fill remaining XP to Level 2, so that different partial XP paths still reach the same progression milestone.
72. As a player, I want to be unable to reach Level 2 before the crisis formally concludes, so that the slice has a clear progression arc.
73. As a player, I want completionist play not to give a significant level advantage, so that the slice does not reward grinding both violence and balance.
74. As a player, I want Level 2 to trigger after the Elder's Reflection Conversation, so that progression follows consequence reflection.
75. As a player, I want the Elder to ask what I learned, so that the First Improvement is framed diegetically.
76. As a player, I want to choose Vitality Improvement by saying I must endure more, so that extra health feels tied to character growth.
77. As a player, I want to choose Melee Improvement by saying I must strike more surely, so that better combat feels tied to character growth.
78. As a player, I want to choose Observation Improvement by saying I must see what others miss, so that non-combat growth feels meaningful.
79. As a player, I want all three First Improvements to be available after any Crisis Outcome, so that my future development is not locked by this crisis.
80. As a player, I want Observation Improvement to have a concrete post-level-up demonstration, so that it does not feel like only future value.
81. As a player, I want the slice to fade to the Kindred title after the First Improvement choice, so that the experience has a clear ending.
82. As a tester, I want options to continue exploring, replay, or exit after the ending, so that the slice supports development and playtest workflows.
83. As a player, I want the world to remain in its changed state if I continue exploring, so that the Persistent Consequence remains visible.
84. As a player, I want Defeat at zero health to incapacitate rather than permanently kill me, so that I can recover and continue the slice.
85. As a player, I want discovered signs, completed actions, XP, levels, and essential equipment to persist after Defeat, so that failure does not erase progress.
86. As a player, I want Defeat to carry a small cost, so that danger still matters.
87. As a player, I want Bandages for immediate healing, so that I can recover during or after dangerous encounters.
88. As a player, I want Food to grant gradual health recovery over time, so that preparation matters without adding a hunger system.
89. As a player, I want to start with a small number of Bandages and Food, so that resource use is introduced simply.
90. As a player, I want a limited resource cache from the Village or Threatened Villager, so that I can recover from mistakes without shops or crafting.
91. As a player, I want emergency help to prevent a locked state if resources run out, so that the slice remains completable.
92. As a player, I want no shop, crafting, hunger penalty, or economy in the slice, so that the focus stays on Forest Crisis.
93. As a player, I want NPCs to address me through tone and relational words rather than a fixed name, so that future character creation remains open.
94. As a player, I want the Elder's knowledge to feel practical and tradition-bearing with a spiritual tone, so that Kindred feels like Nordic fantasy without using visible magic to solve the crisis.
95. As a player, I want the crisis to remain understandable through observation, so that no hidden supernatural explanation invalidates my reasoning.
96. As a player, I want dialogue examples to preserve Nordic tone, so that the fantasy identity comes through without requiring spell effects.
97. As a player, I want to type what I want to say to central NPCs, so that dialogue feels expressive.
98. As a player, I want the Threatened Villager to understand simple help-seeking, clarification, and questions, so that the first dialogue proves basic Intent-Based Free Text.
99. As a player, I want the Elder to understand reports of discovered signs and requests for interpretation, so that free text can support reasoning.
100. As a player, I want the Logger Foreperson to understand explanations and compromise proposals only when context allows, so that persuasion respects world state.
101. As a player, I want reasonable phrasing variation, short answers, and minor spelling errors to map to intended Dialogue Intentions, so that I do not need secret keywords.
102. As a player, I want ambiguous or unknown input to receive diegetic fallback responses, so that limitations feel like NPC understanding rather than parser failure.
103. As a developer, I want each conversation to expose a small set of allowed Dialogue Intentions, so that free text remains controlled and testable.
104. As a developer, I want the Intention Classifier to be deterministic for the vertical slice, so that dialogue behavior is reproducible and offline-friendly.
105. As a developer, I want the Intention Classifier to be replaceable later, so that an LLM-based classifier can be explored without rewriting world or quest logic.
106. As a developer, I want dialogue flow to depend on internal Dialogue Intentions and world state, so that visible text is never used as logic.
107. As a developer, I want player-facing dialogue and Observation Text in localizable resources, so that localization does not require untangling hardcoded content.
108. As a developer, I want stable text keys for all localizable player-facing text, so that language changes do not break quest or dialogue behavior.
109. As a developer, I want tests at the Forest Crisis scenario seam, so that outcome rules and progression are verified through player-visible behavior.
110. As a developer, I want tests for the dialogue intention seam, so that free-text behavior remains controlled across contexts.
111. As a developer, I want tests for localization boundaries, so that visible text cannot become a hidden dependency.
112. As a designer, I want Forest Crisis documented with canonical glossary terms, so that future tickets do not drift into "wolf quest" or combat-only progression.

## Implementation Decisions

- The feature is named Forest Crisis across documentation, code, stories, and tickets.
- The first implementation should preserve the current Java 21 Gradle multi-module structure.
- The `game` application remains the owner of the playable slice, authored content, scenario wiring, and runtime composition.
- The `engine` module should continue to own reusable ECS, UI, input, combat, XP, and interaction primitives.
- New Forest Crisis behavior should be introduced at a high-level scenario/world-state seam above low-level ECS mechanics wherever possible.
- Existing movement, collision, combat, health, XP, level-up, interaction, UI, and input systems should be reused where they fit.
- Avoid spreading Forest Crisis rules directly through unrelated systems when a scenario/state coordinator can express the rules more clearly.
- The crisis has exactly three legitimate Crisis Outcomes: Combat Outcome, Balance Outcome, and Mixed Outcome.
- Combat Outcome is determined by at least two of three Wolves being defeated and zero Balance actions completed.
- Balance Outcome is determined by both Balance actions being completed, regardless of earlier combat.
- Mixed Outcome is determined by at least one Predator Defeat and exactly one completed Balance action.
- Completing one Balance action without any Predator Defeat does not complete the crisis.
- The outcome priority is Balance Outcome first, Mixed Outcome second, Combat Outcome third.
- Predator Defeat means the predator no longer contributes to the immediate threat; the vertical slice may implement this through current combat defeat only, while leaving room for future non-lethal methods.
- Balance Outcome must be reachable without defeating any enemy, in accordance with ADR 0001.
- The Balance Path remains dangerous through Threat Zones, readable Wolf behavior, route choice, timing, positioning, retreat, and Safe Places.
- Breaking Contact ends an active predator encounter by leaving the Threat Zone, maintaining enough distance briefly, or reaching a Safe Place.
- Wolves must not be draggable indefinitely away from their area.
- The three required main signs are Abandoned Grazing Site, Cleared Shelter, and Predator Trail.
- The Player must find all three main signs and complete the Elder Interpretation Conversation before the Logger Foreperson can be convinced.
- There is no automatic journal, clue checklist, or objective counter for signs.
- Observation Text may appear when examining signs, but it must not create a visible checklist or objective marker.
- Internally, examined signs are stored as world state and can affect dialogue and crisis progression.
- Each required sign must be revisitable and communicated by at least two independent signals: one environmental signal and one NPC or Elder hint.
- The Elder has at least two active conversation roles: Interpretation Conversation during investigation and Reflection Conversation after a Crisis Outcome is ready.
- The Elder must not provide missing evidence or reveal the concrete solution.
- The Logger Foreperson can be approached before the Player has evidence, but persuasion is blocked by a Diegetic Gate.
- Failed persuasion must be recoverable and must not permanently block the Balance Path.
- Previous tone or pressure may affect NPC tone but not permanent availability of the compromise.
- The Forestry Compromise protects the Damaged Grove while allowing logging to continue at the Alternative Logging Area.
- The two Balance actions are marking the Alternative Logging Area and performing Habitat Restoration at the Damaged Grove.
- Habitat Restoration can be represented as a small, authored set of interactions rather than a general construction or resource system.
- The crisis becomes ready to conclude when an outcome condition is met, but formally concludes only through the Elder Reflection Conversation.
- The immediate world state should be visible before the final Elder reflection.
- After reflection, partial XP is summarized and final XP fills the remaining amount needed for Level 2.
- All legitimate outcomes must produce Level 2 through Progression Parity.
- Extra optional actions may provide minor partial XP but must not allow Level 2 before crisis conclusion or a major level advantage afterward.
- The First Improvement choice occurs after Level 2 and before the final fade to the Kindred title.
- Vitality Improvement, Melee Improvement, and Observation Improvement are always available regardless of Crisis Outcome.
- Observation Improvement should have a small concrete demonstration in the slice, such as an extra observation or final Elder line.
- Defeat at zero health should incapacitate the Player, return them to a Safe Place, restore health, and preserve discovered signs, actions, XP, levels, and essential equipment.
- Defeat should have only a small cost in the slice, such as resource loss or non-mechanical equipment wear feedback.
- The slice includes Bandages for immediate healing and Food for gradual health recovery over time.
- The Player starts with a small number of Bandages and Food and can access a small limited cache.
- No shops, crafting, hunger penalties, or broader economy are included in this slice.
- Intent-Based Free Text is a core dialogue principle for Kindred, in accordance with ADR 0003.
- The vertical slice's full Intent-Based Free Text scope is limited to the Threatened Villager, Elder, and Logger Foreperson.
- Background NPCs may have simple greetings, fallback lines, or no dialogue.
- Each central conversation exposes a small set of controlled Dialogue Intentions based on NPC role, conversation context, and world state.
- The first bounded set of controlled Dialogue Intentions for the vertical slice includes `greet`, `ask_capabilities`, `ask_for_help`, `clarify`, `report_predator_tracks`, `ask_about_logging`, `explain_ecological_link`, `propose_compromise`, `choose_observation_improvement`, and `goodbye`.
- This first intention set is not exhaustive and is not a promise of open-ended dialogue; allowed intentions still depend on NPC, conversation context, and world state.
- The Intention Classifier for the vertical slice is deterministic and rule-based, but must remain replaceable by a later classifier without requiring quest, dialogue, or world-state logic rewrites.
- Recognizing an intention does not guarantee that it has an effect; world-state gating still determines whether the corresponding dialogue node or action is available.
- Ambiguous, unknown, or out-of-context input must produce diegetic fallback responses.
- The system must never invent new topics, facts, quests, or consequences from player free text.
- All playable dialogue and other localizable player-facing text must live in separate language resources and be retrieved through stable text keys, in accordance with ADR 0002.
- Quest logic, dialogue flow, ECS components, and world-state logic must reference internal IDs rather than visible text.
- Design and examples from the current planning discussion are in Swedish; playable implementation dialogue should be English if English remains the project's game language.
- NPCs should avoid a fixed Player name and instead use tone or relational address.
- Fantasy tone should come from Nordic environment, architecture, clothing, language tone, symbols, local traditions, and the Elder's spiritual-but-practical worldview rather than visible magical effects.
- The vertical slice ends clearly after the First Improvement choice with a fade to the Kindred title.
- Development and playtest builds may offer Continue Exploring, Replay, or Exit; continuing should preserve the changed world state.

## Testing Decisions

- The primary test seam is the Forest Crisis scenario seam at the highest practical level above ECS internals.
- Forest Crisis scenario tests should drive world state and player actions and assert externally visible outcomes rather than implementation details of individual components.
- The main scenario seam must verify the three locked outcome rules: Combat Outcome requires at least two of three Wolves defeated and zero Balance actions, Balance Outcome requires both Balance actions, and Mixed Outcome requires at least one Wolf defeated plus exactly one Balance action.
- The main scenario seam must verify outcome priority: both Balance actions produce Balance Outcome even if combat occurred earlier.
- The main scenario seam must verify that one Balance action without Predator Defeat does not complete the crisis.
- The main scenario seam must verify that Balance Outcome can be reached without any defeated enemies.
- The main scenario seam must verify that all three legitimate outcomes lead to Level 2.
- The main scenario seam must verify that the final reward fills remaining XP differences so partial-XP variance does not break Progression Parity.
- The main scenario seam must verify that Vitality Improvement, Melee Improvement, and Observation Improvement are always selectable after any legitimate outcome.
- The main scenario seam must verify that the Logger Foreperson cannot be convinced before all three main signs are found and interpreted with the Elder.
- The main scenario seam must verify that failed early persuasion is recoverable after required knowledge is gained.
- The main scenario seam must verify that each outcome produces distinct world state and NPC reaction data.
- The dialogue intention seam must treat Intent-Based Free Text as a core principle while limiting slice coverage to the Threatened Villager, Elder, and Logger Foreperson.
- The dialogue intention seam must test deterministic classification from multiple natural player phrasings to the same controlled Dialogue Intention.
- The dialogue intention seam must test reasonable short answers and spelling mistakes where supported by authored rules.
- The dialogue intention seam must test context and world-state gating, including known intentions that are recognized but not allowed to progress yet.
- The dialogue intention seam must test ambiguous and unknown input.
- The dialogue intention seam must test diegetic fallback responses rather than parser-error style messages.
- The dialogue intention seam must test that player free text cannot create new facts, quests, or consequences outside authored intentions.
- The localization seam must verify that player-facing dialogue, Observation Text, prompts, fallback responses, and final lines are resolved from stable text keys.
- The localization seam must verify that visible text is never used as a logic identifier.
- Existing prior art is limited: the repo has JUnit 5 configured for all subprojects but no existing tests.
- Existing systems provide useful setup patterns for tests: EntityManager, ExperienceSystem, CombatSystem, InteractionSystem, StatCalculationSystem, and UI text/input components.
- Prefer testing the `game` scenario behavior where Forest Crisis is authored, with lower-level `engine` tests only for reusable primitives such as deterministic intention classification or localization resolution if those primitives live in `engine`.
- Verification for implementation should include `./gradlew test` and `./gradlew build`.

## Out of Scope

- A full MMORPG implementation.
- Multiplayer, networking behavior, accounts, persistence services, or server authority.
- A full quest log, clue journal, objective tracker, minimap markers, or checklist UI.
- Shops, crafting, hunger penalties, full economy, or repeatable resource farming.
- Complex reputation systems or permanent faction hostility.
- Permanent Player death, XP loss, level loss, or essential equipment loss.
- Non-lethal Predator Defeat mechanics unless separately chosen during implementation; the slice only needs methods supported by its actual combat/encounter systems.
- A general construction, harvesting, or forestry simulation.
- Procedural ecosystems or long-term ecological simulation.
- Open-ended AI conversation.
- LLM-based dialogue classification in the first implementation.
- Full localization into multiple languages, beyond the architecture and resource boundary required for localizable text.
- Full character creation, fixed protagonist background, or locked player name.
- Supernatural causes, visible magic solutions, corruption twists, or monster lore for this first crisis.
- Additional Crisis Outcomes beyond Combat Outcome, Balance Outcome, and Mixed Outcome.
- Full final world continuation beyond a post-slice development/playtest option.

## Further Notes

Relevant ADRs:

- ADR 0001: Nonviolent Main Outcome.
- ADR 0002: Localizable Player-Facing Text.
- ADR 0003: Intent-Based Free-Text Dialogue.

Canonical domain vocabulary is in `CONTEXT.md`. The spec intentionally uses terms such as Forest Crisis, Balance Path, Predator Defeat, Forestry Compromise, Habitat Restoration, Diegetic Gate, Progression Parity, First Improvement, Intent-Based Free Text, Dialogue Intention, and Intention Classifier.

The current runtime is manually wired in the `game` application. When implementing, prefer the smallest correct seams that make Forest Crisis testable without turning every domain rule into an ECS component. The highest-value implementation shape is likely a scenario/world-state layer that coordinates existing engine systems and authored content.

## Publication Status

This spec is published as GitHub issue #2 with the `ready-for-agent` label.
