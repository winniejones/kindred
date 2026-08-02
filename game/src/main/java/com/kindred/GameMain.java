package com.kindred;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.systems.*;
import com.kindred.engine.input.InputState;
import com.kindred.engine.input.Keyboard;
import com.kindred.engine.level.Level;
import com.kindred.engine.level.MapLoader;
import com.kindred.engine.level.SpawnPoint;
import com.kindred.engine.render.Screen;
import com.kindred.engine.resource.AnimationDataRegistry;
import com.kindred.engine.resource.AssetLoader;
import com.kindred.engine.ui.UIManager;
import com.kindred.engine.ui.layout.DefaultGameUILayout;
import com.kindred.game.forest.*;
import com.kindred.game.text.PlayerTextKey;
import com.kindred.game.text.PlayerTextResolver;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
public class GameMain extends Canvas implements Runnable, MouseMotionListener {

    // --- Member Variables ---
    private JFrame frame;
    private Thread gameThread;
    private boolean running = false;

    // Game Window Constants
    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 500;
    public static final int SCALE = 2;
    private static final PlayerTextResolver PLAYER_TEXT = PlayerTextResolver.forLocale(Locale.ENGLISH);
    public static final String TITLE = PLAYER_TEXT.resolve(PlayerTextKey.TITLE_KINDRED);

    // Rendering Buffer
    private final BufferedImage image;
    private final int[] pixels;

    // Core Engine Components
    private final Screen screen;
    private final Keyboard keyboard;
    private final EntityManager entityManager;
    private final Level level;
    private final InputState inputState = new InputState();
    public static AnimationDataRegistry animationRegistry; // +++ NEW: AnimationDataRegistry instance

    // Systems - Declare all systems used
    private final MovementSystem movementSystem;
    private final AnimationSystem animationSystem;
    private final RenderSystem renderSystem;
    private final CameraSystem cameraSystem;
    private final CollisionSystem collisionSystem;
    private final PlayerInputSystem playerInputSystem;
    private final DebugRenderSystem debugRenderSystem;
    private final AISystem aiSystem;
    private final CombatSystem combatSystem;
    private final VisualEffectsSystem visualEffectsSystem;
    private final LifetimeSystem lifetimeSystem;
    private final ParticlePhysicsSystem particlePhysicsSystem;
    private final CorpseDecaySystem corpseDecaySystem;
    private final ExperienceSystem experienceSystem;
    private final StatCalculationSystem statCalculationSystem;
    private final InteractionSystem interactionSystem;
    private final UIManager uiManager;
    private final DefaultGameUILayout gameUILayout;
    private final ForestCrisisState forestCrisisState;
    private final ForestCrisisGreybox forestCrisisGreybox;
    private final ForestCrisisIntroductionPath forestCrisisIntroductionPath;
    private final ForestCrisisWolfEncounter forestCrisisWolfEncounter;
    private final ForestCrisisRecoveryResources forestCrisisRecoveryResources;
    private final ForestCrisisResourceInput forestCrisisResourceInput;
    private final Map<String, Integer> wolfEntityIds = new HashMap<>();
    private final Set<String> recordedWolfDefeats = new HashSet<>();


    // Entity IDs
    private int playerEntity = -1;
    private int cameraEntity;
    private boolean forestCrisisRecoveredPlayerThisFrame;
    private PlayerRecoveryEssentials latestPlayerRecoveryEssentials = PlayerRecoveryEssentials.defaultPlayer();

    /**
     * GameMain Constructor: Initializes the game window, loads assets,
     * creates the level, initializes ECS and systems, and spawns initial entities.
     */
    public GameMain() {
        // --- Window Setup ---
        setPreferredSize(new Dimension(WINDOW_WIDTH * SCALE, WINDOW_HEIGHT * SCALE));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        requestFocusInWindow();

        image = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        // --- Core Component Initialization ---
        screen = new Screen(WINDOW_WIDTH, WINDOW_HEIGHT);
        keyboard = new Keyboard();
        addKeyListener(new GameKeyListener());
        addMouseListener(new MouseInputAdapter());
        addMouseMotionListener(this);

        // --- Initialize AnimationDataRegistry ---
        if (animationRegistry == null) { // Ensure it's loaded once
            animationRegistry = new AnimationDataRegistry();
            log.info("AnimationDataRegistry initialized.");
        }

        // --- Level Loading ---
        log.info("Loading level...");
        level = MapLoader.loadLevelFromImage(ForestCrisisGreybox.LEVEL_RESOURCE, ForestCrisisGreybox.TILE_SIZE);
        log.info("Level loading complete.");

        // --- ECS and System Initialization ---
        log.info("Initializing ECS and Systems...");
        entityManager = new EntityManager();
        movementSystem = new MovementSystem(entityManager);
        // +++ Pass animationRegistry to systems that need it +++
        playerInputSystem = new PlayerInputSystem(entityManager, keyboard, animationRegistry);
        aiSystem = new AISystem(entityManager, animationRegistry);
        // ++++++++++++++++++++++++++++++++++++++++++++++++++++++
        animationSystem = new AnimationSystem(entityManager); // AnimationSystem itself might not need the registry directly
                                                            // if AnimationComponent is populated by input/AI systems.
        renderSystem = new RenderSystem(entityManager, screen);
        cameraSystem = new CameraSystem(entityManager, screen, level);
        collisionSystem = new CollisionSystem(entityManager, level);
        debugRenderSystem = new DebugRenderSystem(entityManager, screen, level);
        combatSystem = new CombatSystem(entityManager);
        visualEffectsSystem = new VisualEffectsSystem(entityManager);
        lifetimeSystem = new LifetimeSystem(entityManager);
        particlePhysicsSystem = new ParticlePhysicsSystem(entityManager);
        experienceSystem = new ExperienceSystem(entityManager);
        statCalculationSystem = new StatCalculationSystem(entityManager);
        interactionSystem = new InteractionSystem(entityManager);
        corpseDecaySystem = new CorpseDecaySystem(entityManager);
        uiManager = new UIManager();
        forestCrisisState = new ForestCrisisState();
        forestCrisisGreybox = ForestCrisisGreybox.createDefault(forestCrisisState);
        forestCrisisIntroductionPath = ForestCrisisIntroductionPath.createDefault(forestCrisisState);
        forestCrisisWolfEncounter = ForestCrisisWolfEncounter.createDefault(forestCrisisGreybox, forestCrisisState);
        forestCrisisRecoveryResources = ForestCrisisRecoveryResources.createDefault();
        forestCrisisResourceInput = new ForestCrisisResourceInput();
        log.info("Systems and UIManager initialized.");

        // --- Initial Entity Spawning ---
        spawnEntitiesFromMap();
        cameraEntity = createCamera();

        // --- Post-Spawn Checks ---
        // Verify that the player was actually spawned from the map data
        if (playerEntity == -1) {
            log.error("CRITICAL: Player entity was not created. No player spawn point found in map? Exiting.");
            throw new RuntimeException("Failed to create player entity - No spawn point found.");
        } else {
            log.info("Player entity successfully created with ID: {}", playerEntity);
            log.info("Performing initial stat calculation...");
            statCalculationSystem.recalculateStats(playerEntity);
            for (int entityId : entityManager.getEntitiesWith(StatsComponent.class)) {
                if (entityId != playerEntity) {
                    statCalculationSystem.recalculateStats(entityId);
                }
            }
            log.info("Initial stat calculation complete.");
        }

        // --- Build UI using Factory ---
        gameUILayout = DefaultGameUILayout.build(uiManager, WINDOW_WIDTH, WINDOW_HEIGHT, entityManager, playerEntity);
        createForestCrisisGreyboxMarkers();
        showIntroductionMoment(forestCrisisIntroductionPath.safeMoment());
        showIntroductionMoment(forestCrisisIntroductionPath.interactionHint());
        gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_STARTING_RESOURCES));
        log.info("GameMain initialization complete.");
    }

    /** Spawns entities based on map markers */
    private void spawnEntitiesFromMap() {
        List<SpawnPoint> spawnPoints = level.getSpawnPoints();
        int tileSize = level.getTileSize();
        boolean playerSpawned = false;

        log.info("Processing {} spawn points...", spawnPoints.size());

        for (SpawnPoint sp : spawnPoints) {
            int spawnX = sp.getTileX() * tileSize + tileSize / 2;
            int spawnY = sp.getTileY() * tileSize + tileSize / 2;

            switch (sp.getType()) {
                case PLAYER:
                    if (!playerSpawned) {
                        this.playerEntity = createPlayer(spawnX, spawnY);
                        playerSpawned = true;
                    } else {
                        log.warn("Multiple player spawn points detected. Ignoring extra at tile ({}, {})", sp.getTileX(), sp.getTileY());
                    }
                    break;
                case NPC_VILLAGER:
                    createVillagerNPC(spawnX, spawnY);
                    break;
                case ENEMY_DEIDARA:
                    createEnemyDeidara(spawnX, spawnY);
                    break;
                default:
                    log.warn("Unknown spawn type encountered in map data: {}", sp.getType());
                    break;
            }
        }
        if (!playerSpawned) {
            log.warn("No PLAYER spawn point was found in the map file!");
            // The check in the constructor will handle the critical error or fallback logic.
        }
    }


    // --- Entity Factory Methods ---

    private int createPlayer(int spawnX, int spawnY) {
        log.debug("Creating Player at: ({}, {})", spawnX, spawnY);
        int entityId = entityManager.createEntity();
        String playerSheetPath = "/assets/sprites/player.png";
        int numDirections = 4;
        int framesPerDirection = 3;
        BufferedImage[][] walkFrames = new BufferedImage[numDirections][framesPerDirection];
        boolean playerSpritesLoaded = false;
        int playerSpriteSize = 32;

        try {
            BufferedImage sheet = AssetLoader.loadImage(playerSheetPath);
            if (sheet != null && sheet.getWidth() >= playerSpriteSize * numDirections && sheet.getHeight() >= playerSpriteSize * framesPerDirection) {
                for (int frameRow = 0; frameRow < framesPerDirection; frameRow++) {
                    for (int dirCol = 0; dirCol < numDirections; dirCol++) {
                        int directionIndex;
                        switch (dirCol) {
                            case 0: directionIndex = AnimationComponent.LEFT; break;  // Col 0 -> LEFT (1)
                            case 1: directionIndex = AnimationComponent.DOWN; break;  // Col 1 -> DOWN (0)
                            case 2: directionIndex = AnimationComponent.UP; break;    // Col 2 -> UP (3)
                            case 3: directionIndex = AnimationComponent.RIGHT; break; // Col 3 -> RIGHT (2)
                            default: continue;
                        }
                        walkFrames[directionIndex][frameRow] = AssetLoader.getSprite(sheet, dirCol, frameRow, playerSpriteSize, playerSpriteSize);
                        if (walkFrames[directionIndex][frameRow] == null || walkFrames[directionIndex][frameRow].getWidth() <= 1) {
                             log.warn("Warning: Failed player sprite load (Col:{}, Row:{}) -> Dir:{}", dirCol, frameRow, directionIndex);
                        }
                    }
                }
                playerSpritesLoaded = true;
                log.debug("Player animation frames loaded using nested loops.");
            } else { /* Error handling */ }
        } catch (Exception e) { log.error("Error loading player spritesheet", e); }

        BufferedImage initialSprite;
        // Use Down direction (index 0) frame 0 as default
        if (playerSpritesLoaded && walkFrames[AnimationComponent.DOWN] != null && walkFrames[AnimationComponent.DOWN][0] != null && walkFrames[AnimationComponent.DOWN][0].getWidth() > 1) {
            initialSprite = walkFrames[AnimationComponent.DOWN][0];
        } else {
            log.warn("Using placeholder for initial player sprite.");
            initialSprite = AssetLoader.createPlaceholderImage(playerSpriteSize, playerSpriteSize);
        }

        entityManager.addComponent(entityId, new PositionComponent(spawnX, spawnY));
        entityManager.addComponent(entityId, new VelocityComponent(0, 0));
        entityManager.addComponent(entityId, new SpriteComponent(initialSprite));
        // Use FPS constructor for AnimationComponent
        entityManager.addComponent(entityId, new AnimationComponent(walkFrames, 7f)); // Example: 7 FPS
        entityManager.addComponent(entityId, new PlayerComponent());
        entityManager.addComponent(entityId, new ColliderComponent(15, 14, 8, 15));
        entityManager.addComponent(entityId, new HealthComponent(100));
        entityManager.addComponent(entityId, new NameComponent("Lolzords"));
        entityManager.addComponent(entityId, new AttackComponent(10f, 45f, 0.5f));
        entityManager.addComponent(entityId, new ExperienceComponent());
        entityManager.addComponent(entityId, new StatsComponent());
        log.info("Player Entity Created with ID: {}", entityId);
        return entityId;
    }

    private int createVillagerNPC(int spawnX, int spawnY) {
        log.debug("Creating Villager NPC at: ({}, {})", spawnX, spawnY);
        int entityId = entityManager.createEntity();
        String sheetPath = "/assets/sprites/blondLong.png"; // TODO: Verify path
        // NPC/Enemy Sheet Layout: Rows=Direction (0=D, 1=L, 2=R, 3=U), Cols=Frame (0-2)
        int numDirections = 4;
        int framesPerDirection = 3;
        BufferedImage[][] walkFrames = new BufferedImage[numDirections][framesPerDirection];
        boolean spritesLoaded = false;
        int spriteSize = 32;

        try {
            BufferedImage sheet = AssetLoader.loadImage(sheetPath);
            if (sheet != null && sheet.getWidth() >= spriteSize * framesPerDirection && sheet.getHeight() >= spriteSize * numDirections) {
                // <<< Nested loop loading for NPC/Enemy Sheet Layout >>>
                for (int dirRow = 0; dirRow < numDirections; dirRow++) {
                    // Map sheet row to AnimationComponent direction constant
                    int directionIndex;
                    switch (dirRow) {
                        case 0: directionIndex = AnimationComponent.DOWN; break;  // Row 0 -> DOWN (0)
                        case 1: directionIndex = AnimationComponent.LEFT; break;  // Row 1 -> LEFT (1)
                        case 2: directionIndex = AnimationComponent.RIGHT; break; // Row 2 -> RIGHT (2)
                        case 3: directionIndex = AnimationComponent.UP; break;    // Row 3 -> UP (3)
                        default: continue;
                    }
                    for (int frameCol = 0; frameCol < framesPerDirection; frameCol++) {
                        walkFrames[directionIndex][frameCol] = AssetLoader.getSprite(sheet, frameCol, dirRow, spriteSize, spriteSize);
                         if (walkFrames[directionIndex][frameCol] == null || walkFrames[directionIndex][frameCol].getWidth() <= 1) {
                             log.warn("Warning: Failed villager sprite load (Col:{}, Row:{}) -> Dir:{}", frameCol, dirRow, directionIndex);
                        }
                    }
                }
                spritesLoaded = true;
            } else { /* Error handling */ }
        } catch (Exception e) { log.error("Error loading villager sprites", e); }

        BufferedImage initialSprite;
        if (spritesLoaded && walkFrames[AnimationComponent.DOWN][0] != null && walkFrames[AnimationComponent.DOWN][0].getWidth() > 1) {
            initialSprite = walkFrames[AnimationComponent.DOWN][0];
        } else { initialSprite = AssetLoader.createPlaceholderImage(spriteSize, spriteSize); }

        // Components
        entityManager.addComponent(entityId, new PositionComponent(spawnX, spawnY));
        entityManager.addComponent(entityId, new VelocityComponent(0, 0));
        entityManager.addComponent(entityId, new SpriteComponent(initialSprite));
        entityManager.addComponent(entityId, new AnimationComponent(walkFrames, 5f));
        entityManager.addComponent(entityId, new ColliderComponent(20, 28, 6, 4));
        entityManager.addComponent(entityId, new HealthComponent(100));
        entityManager.addComponent(entityId, new NPCComponent());
        entityManager.addComponent(entityId, new InteractableComponent(40f));
        entityManager.addComponent(entityId, new NameComponent("Graze"));
        entityManager.addComponent(entityId, new WanderAIComponent(spawnX, spawnY, 64f, 3.0f, 8.0f, 0.8f, 100f));
        log.debug("Villager NPC Entity Created with ID: {}", entityId);
        return entityId;
    }

    private int createEnemyDeidara(int spawnX, int spawnY) {
        log.debug("Creating Enemy at: ({}, {})", spawnX, spawnY);
        int entityId = entityManager.createEntity();
        String sheetPath = "/assets/sprites/deidara.png"; // TODO: Verify path
        int numDirections = 4; int framesPerDirection = 3;
        BufferedImage[][] walkFrames = new BufferedImage[numDirections][framesPerDirection];
        boolean spritesLoaded = false; int spriteSize = 32;
        try {
            BufferedImage sheet = AssetLoader.loadImage(sheetPath);
            if (sheet != null && sheet.getWidth() >= spriteSize * framesPerDirection && sheet.getHeight() >= spriteSize * numDirections) {
                 // <<< Nested loop loading for NPC/Enemy Sheet Layout >>>
                for (int dirRow = 0; dirRow < numDirections; dirRow++) {
                    int directionIndex;
                    switch (dirRow) { case 0: directionIndex = AnimationComponent.DOWN; break; case 1: directionIndex = AnimationComponent.LEFT; break; case 2: directionIndex = AnimationComponent.RIGHT; break; case 3: directionIndex = AnimationComponent.UP; break; default: continue; }
                    for (int frameCol = 0; frameCol < framesPerDirection; frameCol++) {
                        walkFrames[directionIndex][frameCol] = AssetLoader.getSprite(sheet, frameCol, dirRow, spriteSize, spriteSize);
                        if (walkFrames[directionIndex][frameCol] == null || walkFrames[directionIndex][frameCol].getWidth() <= 1) { log.warn("Warning: Failed enemy sprite load (Col:{}, Row:{}) -> Dir:{}", frameCol, dirRow, directionIndex); }
                    }
                }
                spritesLoaded = true;
            } else { /* Error handling */ }
        } catch (Exception e) { log.error("Error loading enemy sprites", e); }

        BufferedImage initialSprite;
        if (spritesLoaded && walkFrames[AnimationComponent.DOWN][0] != null && walkFrames[AnimationComponent.DOWN][0].getWidth() > 1) { initialSprite = walkFrames[AnimationComponent.DOWN][0]; }
        else { initialSprite = AssetLoader.createPlaceholderImage(spriteSize, spriteSize); }

        // Components
        entityManager.addComponent(entityId, new PositionComponent(spawnX, spawnY));
        entityManager.addComponent(entityId, new VelocityComponent(0, 0));
        entityManager.addComponent(entityId, new SpriteComponent(initialSprite));
        entityManager.addComponent(entityId, new AnimationComponent(walkFrames, 12));
        // Use frameDelay
        entityManager.addComponent(entityId, new ColliderComponent(24, 16, 4, 16));
        entityManager.addComponent(entityId, new HealthComponent(30));
        entityManager.addComponent(entityId, new EnemyComponent());
        entityManager.addComponent(entityId, new NameComponent("Deidara"));
        entityManager.addComponent(entityId, new AttackComponent(5f, 35f, 1.5f));
        entityManager.addComponent(entityId, new WanderAIComponent(spawnX, spawnY, 48f, 1.0f, 4.0f, 0.6f, 120f));
        entityManager.addComponent(entityId, new XPValueComponent(15));
        entityManager.addComponent(entityId, new ParticipantComponent());
        entityManager.addComponent(entityId, new StatsComponent(5, 8, 2, 5));
        log.debug("Enemy Entity Created with ID: {}", entityId);
        return entityId;
    }

    private int createCamera() {
        cameraEntity = entityManager.createEntity();
        entityManager.addComponent(cameraEntity, new CameraComponent(0, 0));
        log.debug("Camera Entity Created with ID: {}", cameraEntity);
        return cameraEntity;
    }

    // --- Game Loop and Core Methods ---
    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
        log.info("Game thread started.");
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            log.info("Attempting to stop game thread...");
            gameThread.join(); // Wait for the thread to finish
            log.info("Game thread stopped.");
        } catch (InterruptedException e) {
            log.error("Error stopping game thread", e);
            Thread.currentThread().interrupt();
        }
    }

    /** Main game loop logic (fixed time step). */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerUpdate = 1_000_000_000.0 / 60.0; // Target 60 updates per second
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0, updates = 0;
        log.info("Game loop starting...");

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            // Prevent spiral of death if lagging badly
            if (elapsed < 0) elapsed = 0;
            if (elapsed > nsPerUpdate * 10) elapsed = (long) (nsPerUpdate * 10); // Cap max elapsed time
            lastTime = now;
            delta += elapsed / nsPerUpdate;

            boolean updated = false; // Track if any updates happened

            // Process updates based on elapsed time
            // Limit updates per frame to prevent potential freezes if lagging severely
            int maxUpdatesPerFrame = 5;
            int updatesThisCycle = 0;
            while (delta >= 1 && updatesThisCycle < maxUpdatesPerFrame) {
                // Calculate deltaTime in seconds for this update tick
                float deltaTime = (float) nsPerUpdate / 1_000_000_000.0f;
                update(deltaTime); // Pass delta time to update logic
                updates++;
                delta--;
                updated = true; // Mark that an update occurred
                updatesThisCycle++;
            }
            // If loop exited due to maxUpdates, reset delta partially to avoid losing time entirely
            if (updatesThisCycle == maxUpdatesPerFrame && delta > 1) {
                log.warn("Falling behind! Skipped {} updates.", (int) delta);
                delta = 1; // Allow at least one update next cycle if still lagging
            }

            // Render if an update occurred (or based on separate rendering timer if needed)
            if (updated) {
                render();
                frames++;
            } else {
                // Yield if no work done to prevent busy-waiting
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }


            // Update FPS/UPS counter every second
            if (System.currentTimeMillis() - timer >= 1000) {
                log.debug("UPS: {}, FPS: {}", updates, frames); // Log FPS/UPS
                if (frame != null) {
                    frame.setTitle(TITLE + " | UPS: " + updates + ", FPS: " + frames);
                }
                updates = 0;
                frames = 0;
                timer += 1000;
            }
        }
        // Consider calling a cleanup method here if needed before exit
    }

    /**
     * Updates all game logic and systems for one tick.
     * @param deltaTime Time elapsed since the last update in seconds.
     */
    private void update(float deltaTime) {
        forestCrisisRecoveredPlayerThisFrame = false;
        keyboard.update();

        // --- Update Systems in Order ---
        playerInputSystem.update(deltaTime);
        aiSystem.update(deltaTime); // AI now handles attacks
        updateForestCrisisWolves();
        interactionSystem.update(deltaTime);
        rememberPlayerRecoveryEssentials();
        combatSystem.update(deltaTime);
        recoverPlayerFromForestCrisisDefeat();
        recordForestCrisisWolfDefeats();
        experienceSystem.update(deltaTime);
        statCalculationSystem.update(deltaTime);
        updateForestCrisisFoodRecovery(deltaTime);
        particlePhysicsSystem.update(deltaTime);
        collisionSystem.update(deltaTime);
        movementSystem.update(deltaTime);
        lifetimeSystem.update(deltaTime);    // Update particle lifetimes, destroy expired entities
        visualEffectsSystem.update(deltaTime); // Update flash timers for TookDamageComponent
        corpseDecaySystem.update(deltaTime);
        cameraSystem.update(deltaTime);
        animationSystem.update(deltaTime);   // +++ IMPORTANT: AnimationSystem now needs deltaTime +++

        uiManager.update(inputState, deltaTime);

        if (gameUILayout != null && gameUILayout.isChatInputFocused()) {
            String submitted = gameUILayout.getSubmittedChatTextAndClear();
            if (submitted != null) {
                log.info("Chat Submitted: {}", submitted);
                gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.CHAT_PLAYER_PREFIX) + submitted);
            }
        }
        inputState.clearFrameEvents();
        // ----------------------------------
    }

    /** Renders the current game state. */
    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) { createBufferStrategy(3); return; }
        // Prepare screen for drawing
        screen.clear();

        level.render(screen);
        renderSystem.render();
        debugRenderSystem.render();
        renderForestCrisisGreyboxAreas();

        // --- Draw buffer to screen ---
        System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);

        // Get Graphics and Render UI
        Graphics g = null;
        try {
            g = image.getGraphics(); // Get graphics for the image we draw to the canvas
            if (g != null) {
                // --- Render UI Layer ---
                uiManager.render(g);
            }
        } catch (Exception e) {
            log.error("Error during rendering graphics", e);
        } finally {
            if (g != null) {
                g.dispose(); // Dispose graphics context
            }
        }

        // 5. Draw the final image (with game + UI) to the screen (Canvas)
        Graphics screenGraphics = null;
        try {
            screenGraphics = bs.getDrawGraphics(); // Get graphics for the actual canvas buffer
            if (screenGraphics != null) {
                screenGraphics.drawImage(image, 0, 0, getWidth(), getHeight(), null); // Draw scaled image
            }
        } catch (Exception e) {
            log.error("Error drawing buffer to screen", e);
        } finally {
            if (screenGraphics != null) {
                screenGraphics.dispose();
            }
        }

        // 6. Show the buffer
        try {
            if (!bs.contentsLost()) {
                bs.show();
            } else {
                log.warn("Buffer contents lost.");
            }
        } catch (IllegalStateException e) {
            log.error("BufferStrategy error on show()", e);
        }
    }

    private void interactWithForestCrisisGreybox() {
        if (tryInteractWithForestCrisisEmergencyCache()) {
            return;
        }
        forestCrisisGreybox.interactAt(currentPlayerPoint()).ifPresent(this::showIntroductionMoment);
    }

    private boolean tryInteractWithForestCrisisEmergencyCache() {
        if (!isCurrentPlayerWithinRange(forestCrisisGreybox.safePlaceCenter())) {
            return false;
        }
        showForestCrisisResourceUse(forestCrisisRecoveryResources.claimEmergencyCache());
        return true;
    }

    private boolean isCurrentPlayerWithinRange(GreyboxPoint point) {
        return currentPlayerPoint().distanceSquaredTo(point) <= ForestCrisisGreybox.INTERACTION_RANGE * ForestCrisisGreybox.INTERACTION_RANGE;
    }

    private void showIntroductionMoment(IntroductionMoment moment) {
        gameUILayout.addChatLine(PLAYER_TEXT.resolve(moment.textKey()));
    }

    private GreyboxPoint currentPlayerPoint() {
        PositionComponent position = entityManager.getComponent(playerEntity, PositionComponent.class);
        if (position == null) {
            return forestCrisisGreybox.playerStart();
        }
        ColliderComponent collider = entityManager.getComponent(playerEntity, ColliderComponent.class);
        if (collider == null) {
            return new GreyboxPoint(position.x, position.y);
        }
        return new GreyboxPoint(
                position.x + collider.offsetX + collider.hitboxWidth / 2,
                position.y + collider.offsetY + collider.hitboxHeight / 2);
    }

    private void createForestCrisisGreyboxMarkers() {
        for (GreyboxMarker marker : forestCrisisGreybox.markers()) {
            if (isWolfMarker(marker)) {
                continue;
            }
            createGreyboxMarker(marker);
        }
        createForestCrisisWolfMarkers();
    }

    private boolean isWolfMarker(GreyboxMarker marker) {
        for (WolfPlaceholder wolf : forestCrisisWolfEncounter.wolves()) {
            if (wolf.spawnPosition().equals(marker.position())) {
                return true;
            }
        }
        return false;
    }

    private void createForestCrisisWolfMarkers() {
        for (WolfPlaceholder wolf : forestCrisisWolfEncounter.wolves()) {
            int entityId = createGreyboxMarker(new GreyboxMarker(wolf.spawnPosition(), 0xFF777777, false));
            entityManager.addComponent(entityId, new EnemyComponent());
            entityManager.addComponent(entityId, new ColliderComponent(16, 16));
            entityManager.addComponent(entityId, new HealthComponent(20));
            entityManager.addComponent(entityId, new AttackComponent(4f, ForestCrisisWolfEncounter.WOLF_ATTACK_RANGE, 1.0f));
            entityManager.addComponent(entityId, new NameComponent(wolf.id()));
            entityManager.addComponent(entityId, new ParticipantComponent());
            entityManager.addComponent(entityId, new XPValueComponent(0));
            wolfEntityIds.put(wolf.id(), entityId);
        }
        validateForestCrisisWolfEntities();
    }

    private void validateForestCrisisWolfEntities() {
        for (WolfPlaceholder wolf : forestCrisisWolfEncounter.wolves()) {
            Integer entityId = wolfEntityIds.get(wolf.id());
            if (entityId == null || !entityManager.isEntityActive(entityId)) {
                throw new IllegalStateException("Forest Crisis wolf entity missing for " + wolf.id());
            }
            NameComponent name = entityManager.getComponent(entityId, NameComponent.class);
            if (name == null || !wolf.id().equals(name.name)) {
                throw new IllegalStateException("Forest Crisis wolf entity ID mismatch for " + wolf.id());
            }
            log.info("Forest Crisis wolf {} wired to entity {} with home area {}", wolf.id(), entityId, wolf.homeArea());
        }
    }

    private int createGreyboxMarker(GreyboxMarker marker) {
        int entityId = entityManager.createEntity();
        entityManager.addComponent(entityId, new PositionComponent(marker.position().x(), marker.position().y()));
        entityManager.addComponent(entityId, new SpriteComponent(createColorSprite(marker.color())));
        if (marker.interactable()) {
            entityManager.addComponent(entityId, new InteractableComponent(ForestCrisisGreybox.INTERACTION_RANGE));
        }
        return entityId;
    }

    private void updateForestCrisisWolves() {
        WolfEncounterUpdate update = forestCrisisWolfEncounter.update(currentPlayerPoint());
        showWolfEncounterMoment(update.event());

        for (WolfRuntimeState wolf : update.wolves()) {
            Integer entityId = wolfEntityIds.get(wolf.id());
            if (entityId == null || !entityManager.isEntityActive(entityId) || entityManager.hasComponent(entityId, DeadComponent.class)) {
                continue;
            }
            PositionComponent position = entityManager.getComponent(entityId, PositionComponent.class);
            if (position != null) {
                position.x = wolf.position().x();
                position.y = wolf.position().y();
            }
            if (wolf.state() == WolfState.PURSUING && isWolfInAttackRange(entityId)) {
                AttackComponent attack = entityManager.getComponent(entityId, AttackComponent.class);
                if (attack != null && attack.currentCooldown <= 0) {
                    entityManager.addComponent(entityId, new AttackActionComponent());
                    attack.currentCooldown = attack.attackCooldown;
                }
            }
        }
    }

    private boolean isWolfInAttackRange(int wolfEntityId) {
        PositionComponent wolfPosition = entityManager.getComponent(wolfEntityId, PositionComponent.class);
        PositionComponent playerPosition = entityManager.getComponent(playerEntity, PositionComponent.class);
        AttackComponent attack = entityManager.getComponent(wolfEntityId, AttackComponent.class);
        if (wolfPosition == null || playerPosition == null || attack == null) {
            return false;
        }
        int dx = playerPosition.x - wolfPosition.x;
        int dy = playerPosition.y - wolfPosition.y;
        return dx * dx + dy * dy <= attack.range * attack.range;
    }

    private void recordForestCrisisWolfDefeats() {
        for (Map.Entry<String, Integer> wolf : wolfEntityIds.entrySet()) {
            if (recordedWolfDefeats.contains(wolf.getKey()) || !entityManager.hasComponent(wolf.getValue(), DeadComponent.class)) {
                continue;
            }
            recordedWolfDefeats.add(wolf.getKey());
            WolfEncounterUpdate update = forestCrisisWolfEncounter.recordDefeat(wolf.getKey());
            update.developmentLogMessage().ifPresent(this::emitForestCrisisDevelopmentLog);
            showWolfEncounterMoment(update.event());
        }
    }

    private void recoverPlayerFromForestCrisisDefeat() {
        HealthComponent health = entityManager.getComponent(playerEntity, HealthComponent.class);
        if (health == null || (health.currentHealth > 0 && !entityManager.hasComponent(playerEntity, DeadComponent.class))) {
            return;
        }
        ForestCrisisDefeatRecovery recovery = forestCrisisRecoveryResources.recoverDefeatedPlayer(
                entityManager,
                playerEntity,
                forestCrisisGreybox,
                latestPlayerRecoveryEssentials);
        if (recovery.event() == ForestCrisisResourceEvent.DEFEAT_RECOVERED) {
            forestCrisisRecoveredPlayerThisFrame = true;
            gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_DEFEAT_RECOVERED));
            showForestCrisisResourceEvent(recovery.costEvent());
            WolfEncounterUpdate update = forestCrisisWolfEncounter.update(currentPlayerPoint());
            showWolfEncounterMoment(update.event());
        }
    }

    private void rememberPlayerRecoveryEssentials() {
        ColliderComponent collider = entityManager.getComponent(playerEntity, ColliderComponent.class);
        AttackComponent attack = entityManager.getComponent(playerEntity, AttackComponent.class);
        if (collider != null && attack != null && !entityManager.hasComponent(playerEntity, DeadComponent.class)) {
            latestPlayerRecoveryEssentials = PlayerRecoveryEssentials.from(collider, attack);
        }
    }

    private void updateForestCrisisFoodRecovery(float deltaTime) {
        if (forestCrisisRecoveredPlayerThisFrame) {
            return;
        }
        HealthComponent health = entityManager.getComponent(playerEntity, HealthComponent.class);
        if (health != null && !entityManager.hasComponent(playerEntity, DeadComponent.class)) {
            forestCrisisRecoveryResources.update(deltaTime, health);
        }
    }

    private void useForestCrisisBandage() {
        if (entityManager.hasComponent(playerEntity, DeadComponent.class)) {
            return;
        }
        HealthComponent health = entityManager.getComponent(playerEntity, HealthComponent.class);
        if (health != null) {
            showForestCrisisResourceUse(forestCrisisRecoveryResources.useBandage(health));
        }
    }

    private void useForestCrisisFood() {
        if (entityManager.hasComponent(playerEntity, DeadComponent.class)) {
            return;
        }
        HealthComponent health = entityManager.getComponent(playerEntity, HealthComponent.class);
        if (health != null) {
            showForestCrisisResourceUse(forestCrisisRecoveryResources.useFood(health));
        }
    }

    private void showForestCrisisResourceUse(ForestCrisisResourceUse use) {
        showForestCrisisResourceEvent(use.event());
    }

    private void showForestCrisisResourceEvent(ForestCrisisResourceEvent event) {
        switch (event) {
            case BANDAGE_USED -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_BANDAGE_USED));
            case FOOD_STARTED -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_FOOD_STARTED));
            case NO_BANDAGES -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_NO_BANDAGES));
            case NO_FOOD -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_NO_FOOD));
            case FOOD_ALREADY_ACTIVE -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_FOOD_ALREADY_ACTIVE));
            case DEFEAT_COST_BANDAGE -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_DEFEAT_COST_BANDAGE));
            case DEFEAT_COST_FOOD -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_DEFEAT_COST_FOOD));
            case DEFEAT_COST_NONE -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_DEFEAT_COST_NONE));
            case EMERGENCY_CACHE_CLAIMED -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_EMERGENCY_CACHE_CLAIMED));
            case EMERGENCY_CACHE_EXHAUSTED -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.RECOVERY_EMERGENCY_CACHE_EXHAUSTED));
            case DEFEAT_RECOVERED, NONE -> {
            }
        }
    }

    private void emitForestCrisisDevelopmentLog(String message) {
        log.info(message);
        System.out.println(message);
    }

    private void showWolfEncounterMoment(WolfEncounterEvent event) {
        switch (event) {
            case WARNING -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.WOLF_ENCOUNTER_WARNING));
            case CONTACT_STARTED -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.WOLF_ENCOUNTER_HOSTILE_CONTACT));
            case CONTACT_BROKEN -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.WOLF_ENCOUNTER_CONTACT_BROKEN));
            case WOLF_DEFEATED -> gameUILayout.addChatLine(PLAYER_TEXT.resolve(PlayerTextKey.WOLF_ENCOUNTER_DEFEATED));
            case NONE -> {
            }
        }
    }

    private BufferedImage createColorSprite(int color) {
        BufferedImage sprite = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = sprite.createGraphics();
        graphics.setColor(new Color(color, true));
        graphics.fillRect(0, 0, 16, 16);
        graphics.dispose();
        return sprite;
    }

    private void renderForestCrisisGreyboxAreas() {
        renderGreyboxArea(forestCrisisGreybox.village(), 0xFF7777AA);
        renderGreyboxArea(forestCrisisGreybox.shepherdsFarm(), 0xFFAA8844);
        renderGreyboxArea(forestCrisisGreybox.threatZone(), 0xFFAA3333);
        renderGreyboxArea(forestCrisisGreybox.safePlace(), 0xFF33AA66);
        for (WolfPlaceholder wolf : forestCrisisWolfEncounter.wolves()) {
            renderGreyboxArea(wolf.warningArea(), 0xFFCCCC44);
            renderGreyboxArea(wolf.contactArea(), 0xFFFF8844);
        }
    }

    private void renderGreyboxArea(GreyboxArea area, int color) {
        screen.drawRect(area.x(), area.y(), area.width(), area.height(), color, true);
    }

    public static void main(String[] args) {
        GameMain game = new GameMain();
        game.frame = new JFrame(TITLE);
        game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.frame.setResizable(false);
        game.frame.add(game);
        game.frame.pack();
        game.frame.setLocationRelativeTo(null);
        game.frame.setVisible(true);
        game.requestFocusInWindow();
        game.start();
    }

    // --- Mouse Listener Methods ---

    // Inner class to handle mouse button presses/releases etc.
    private class MouseInputAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int scale = GameMain.SCALE;
            int logicalX = e.getX() / scale;
            int logicalY = e.getY() / scale;
            inputState.setButtonDown(e.getButton());

            // --- Check for Chat Input Focus on Click ---
            // <<< Need access to chatInput bounds, potentially via Layout object or UIManager >>>
            // This logic might need refinement depending on how UI element bounds are checked
            boolean clickedOnChatInput = false;
            if (gameUILayout != null /* && gameUILayout.isCoordOverChatInput(logicalX, logicalY) */) {
                // If we can check bounds via layout/manager:
                // clickedOnChatInput = true;
                // For now, assume any click outside sidebar potentially unfocuses
            }

            // Simplified logic: If chat is focused and click is outside, unfocus.
            // If click is inside (checked by button itself), button handles focus.
            // This doesn't handle clicking *on* the input field itself perfectly yet.
            if (gameUILayout != null && gameUILayout.isChatInputFocused() && !clickedOnChatInput) {
                // A more robust check would be needed here using UIManager.isMouseOverUI
                // or specific bounds checking if the click wasn't on an interactive element.
                // For now, any click outside *might* unfocus.
                // log.debug("Potential unfocus click outside chat input.");
                // gameUILayout.unfocusChatInput(); // Maybe too aggressive?
            }
            // Button clicks should handle their own focus gain if needed
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int scale = GameMain.SCALE;
            inputState.setButtonUp(e.getButton());
            // updateMousePos(e); // Position updated by move/drag
        }

        // Implement other methods like mouseEntered, mouseExited if needed
        @Override
        public void mouseEntered(MouseEvent e) {
            // Optional: Handle mouse entering the game window
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // Optional: Handle mouse leaving the game window (e.g., reset hover states)
            inputState.updateMousePosition(-1, -1); // Indicate mouse is outside
        }

        private void updateMousePos(MouseEvent e) {
            int s = GameMain.SCALE;
            inputState.updateMousePosition(e.getX() / s, e.getY() / s);
        }
    }

    // Implement MouseMotionListener methods directly in GameMain
    @Override
    public void mouseDragged(MouseEvent e) {
        // Update position while button is held down
        int scale = GameMain.SCALE;
        int logicalX = e.getX() / scale;
        int logicalY = e.getY() / scale;
        inputState.updateMousePosition(logicalX, logicalY);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Update position when mouse moves without buttons pressed
        int scale = GameMain.SCALE;
        int logicalX = e.getX() / scale;
        int logicalY = e.getY() / scale;
        inputState.updateMousePosition(logicalX, logicalY);
    }
    // --- End Mouse Listener Methods ---

    /** Handles Keyboard Events */
    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            boolean ctrl = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
            boolean consumed = false; // Flag to check if UI consumed the event

            // --- Handle Chat Input Focus (Ctrl+Enter) ---
            if (ctrl && keyCode == KeyEvent.VK_ENTER) {
                if (gameUILayout.isChatInputFocused()) {
                    gameUILayout.unfocusChatInput();
                } else {
                    log.debug("Giving focus to chat input via Ctrl+Enter.");
                    gameUILayout.focusChatInput();
                    consumed = true;
                }
                return;
            }
            // --- Handle Chat Input Focus (Enter to Submit/Lose Focus) ---
            else if (keyCode == KeyEvent.VK_ENTER) {
                if (gameUILayout != null && gameUILayout.isChatInputFocused()) {
                    // Let chat widget handle Enter for submission (via handleKeyPress)
                    gameUILayout.handleChatKeyPress(e);
                    // Focus loss happens in update loop after text is processed
                    consumed = true;
                }
            }
            else if (keyCode == KeyEvent.VK_E && gameUILayout != null && !gameUILayout.isChatInputFocused()) {
                interactWithForestCrisisGreybox();
            }
            else if (gameUILayout != null) {
                ForestCrisisResourceAction resourceAction = forestCrisisResourceInput.actionFor(
                        keyCode,
                        gameUILayout.isChatInputFocused(),
                        entityManager.hasComponent(playerEntity, DeadComponent.class));
                if (resourceAction == ForestCrisisResourceAction.USE_BANDAGE) {
                    useForestCrisisBandage();
                } else if (resourceAction == ForestCrisisResourceAction.USE_FOOD) {
                    useForestCrisisFood();
                }
            }

            // --- Pass key press to focused UI element (Chat) ---
            if (gameUILayout != null && gameUILayout.isChatInputFocused()) {
                // Pass event to layout facade which passes to chat widget
                gameUILayout.handleChatKeyPress(e);
                consumed = true; // Consume typing keys
            }

            // --- If UI didn't consume, update Keyboard state for game systems ---
            if (!consumed) {
                keyboard.keyPressed(e); // Update the holder object
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // If chat doesn't have focus, update Keyboard state
            if (gameUILayout == null || !gameUILayout.isChatInputFocused()) {
                keyboard.keyReleased(e);
            }
            // Note: No need to pass release events to chatInput usually
        }
    }
    // --- End Input Listener Inner Classes ---
}
