# Kindred - 2D Java Game Project

## Description

Kindred is a 2D game developed in Java, built upon a custom-designed Entity-Component-System (ECS) architecture. The project is organized into several modules, including a core engine, the game-specific logic, networking capabilities (currently a placeholder), and tools. It features systems for rendering, player input, AI, combat, animations, UI management, and character progression including experience and leveling.

## Features

* **Entity-Component-System (ECS) Architecture:** The core of the game is built using an ECS pattern, promoting modularity and separation of concerns.
* **2D Rendering Engine:** Custom rendering system for displaying levels, sprites, particles, and UI elements.
* **Player and NPC Management:** Includes components and systems for player characters, non-player characters (NPCs), and enemies.
* **Movement and Collision:** Systems for entity movement and collision detection with level geometry and other entities.
* **Animation System:** Supports sprite-based animations for entities, including directional movement.
* **Combat System:** Entities can engage in combat, dealing damage and managing health. It includes features like hit particles, corpse decay, and damage participation tracking for XP distribution.
* **AI System:** Basic wandering and attacking AI for enemies.
* **Experience and Leveling System:** Players can gain experience points (XP) from defeating enemies and level up.
* **Stats System:** Entities possess base attributes and derived stats that can be affected by leveling.
* **Interaction System:** Allows players to interact with designated entities.
* **Particle System:** Basic particle effects for events like taking damage.
* **UI System:** In-game UI including a sidebar with minimap, player stats, and action buttons, as well as a chat window.
* **Level Loading:** Levels are loaded from image files, with specific colors representing different tiles and spawn points.
* **Logging:** Utilizes SLF4J with a Logback backend for application logging.

## Engine Architecture (Entity-Component-System)

The game engine follows the Entity-Component-System (ECS) design pattern.

* **Entities:** Unique identifiers for game objects (e.g., player, enemies, items). Managed by the `EntityManager`.
* **Components:** Pure data containers that define the properties of entities. Examples include:
    * `PositionComponent`
    * `VelocityComponent`
    * `SpriteComponent`
    * `AnimationComponent`
    * `HealthComponent`
    * `AttackComponent`
    * `PlayerComponent`
    * `EnemyComponent`
    * `ColliderComponent`
    * `ExperienceComponent`
    * `StatsComponent`
* **Systems:** Implement game logic by operating on entities that possess specific sets of components. Examples include:
    * `MovementSystem`
    * `RenderSystem`
    * `CollisionSystem`
    * `CombatSystem`
    * `AISystem`
    * `AnimationSystem`
    * `ExperienceSystem`
    * `StatCalculationSystem`
    * `PlayerInputSystem`

## Modules

The project is structured into the following Gradle modules:

* **`engine`**: Contains the core game engine functionalities, including the ECS framework, rendering, input handling, level management, UI system, and common game systems.
* **`game`**: Implements the specific game logic, entity creation (player, NPCs, enemies), and ties together the engine components to create the gameplay experience. The main entry point (`GameMain.java`) resides here.
* **`networking`**: Placeholder for future networking capabilities.
* **`tools`**: Placeholder for potential utility tools for game development (e.g., map editor, asset converters).

## Getting Started

This is a Gradle-based project. Ensure you have a compatible JDK (GraalVM CE 21 specified in `.idea/misc.xml`) and Gradle installed or use the Gradle wrapper (`gradlew`).

To build the project, you would typically run:
`./gradlew build`

## How to Run

The main entry point for the game is `com.kindred.GameMain` in the `game` module. You can run this class from your IDE or configure a Gradle task to execute it.

## Key Technologies / Libraries

* **Java**: The primary programming language.
* **SLF4J + Logback**: Used for application logging.
* **Lombok**: Used for reducing boilerplate code (e.g., `@Slf4j`).

## Future Work / Considerations

* **Skill System Implementation**: The design document mentions a skill system; this is a potential area for future development.
* **Networking**: The `networking` module is currently a placeholder and would need significant development for MMORPG features.
* **Server-Side Persistence**: For MMORPG functionality, game state saving and loading needs to be handled server-side, typically with a database.
* **Advanced UI Features**: Further enhancements to UI layout, interactivity, and theming.
* **Sound System**: Implementation of sound effects and music.
* **More Content**: Additional levels, enemies, items, and quests.