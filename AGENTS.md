# AGENTS.md

## Project Shape
- Java 21 Gradle multi-module project using the wrapper (`gradle-8.10`); `gradle.properties` pins `org.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64`.
- Modules are declared in `settings.gradle`: `engine`, `game`, `networking`, `tools`.
- `game` is the only configured application module; its Gradle `mainClass` is `com.kindred.GameMain`.
- `game` depends on `engine` and `networking`; `networking` and `tools` currently only contain placeholder `Main` classes.

## Commands
- Build everything: `./gradlew build`.
- Run the game: `./gradlew :game:run`.
- Test everything: `./gradlew test`.
- Test one module: `./gradlew :engine:test` or `./gradlew :game:test`.
- Run a single JUnit 5 test when tests exist: `./gradlew :engine:test --tests 'com.kindred.SomeTest'`.
- There is no separate lint, formatter, typecheck, or CI config in this repo; Gradle compile/test is the verification path.

## Architecture Notes
- Core ECS lives under `engine/src/main/java/com/kindred/engine/entity`: components are data holders, systems implement `com.kindred.engine.entity.core.System#update(float deltaTime)`, and `EntityManager` stores components by concrete component class.
- `GameMain` wires the runtime manually: Swing window, input listeners, map loading, entity spawning, system construction, and the fixed-step game loop are all in `game/src/main/java/com/kindred/GameMain.java`.
- System update order is hard-coded in `GameMain.update`; changes to combat, XP, collision, movement, animation, or UI behavior may depend on that order.
- Assets are loaded from classpath resource paths such as `/assets/sprites/player.png` via `AssetLoader`; keep runtime assets under `engine/src/main/resources/assets` unless module wiring changes.
- Levels are image-driven: `MapLoader` reads `/assets/level/spawn_map.png`, maps exact ARGB colors from `Tile`, records spawn points, and places floor tiles under spawn markers.

## Repo Quirks
- Lombok is used in `engine` and `game` with `compileOnly` plus `annotationProcessor`; do not remove annotation processing when changing Gradle config.
- There are currently no `src/test` files, but all subprojects are configured for JUnit 5.
- Logging config is `engine/src/main/resources/logback.xml` with root level `DEBUG`, so `:game:run` is intentionally verbose.
