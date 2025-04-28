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
import com.kindred.engine.resource.AssetLoader;
import com.kindred.engine.ui.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;

import static com.kindred.engine.resource.AssetLoader.*;

@Slf4j
public class GameMain extends Canvas implements Runnable, MouseMotionListener {

    // --- Member Variables ---
    private JFrame frame;
    private Thread gameThread;
    private boolean running = false;

    // Game Window Constants
    public static final int PANEL_WIDTH = 180;
    public static final int CHAT_HEIGHT = 120;
    public static final int WIDTH = 900;
    public static final int HEIGHT = 500;
    public static final int SCALE = 2;
    public static final String TITLE = "Kindred";

    // Rendering Buffer
    private final BufferedImage image;
    private final int[] pixels;

    // Core Engine Components
    private final Screen screen;
    private final Keyboard keyboard;
    private final EntityManager entityManager;
    private final Level level;
    private final InputState inputState = new InputState();

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
    private final ExperienceSystem experienceSystem; // <<< Added ExperienceSystem instance
    private final StatCalculationSystem statCalculationSystem; // <<< Added StatCalculationSystem instance
    private final InteractionSystem interactionSystem;
    private final UIManager uiManager;
    private UITextInput chatInput;
    private UIChatDisplay chatArea;


    // Entity IDs
    private int playerEntity = -1; // Initialize player entity ID to invalid (-1 indicates not spawned yet)
    private int cameraEntity;      // ID for the camera entity (if used)

    /**
     * GameMain Constructor: Initializes the game window, loads assets,
     * creates the level, initializes ECS and systems, and spawns initial entities.
     */
    public GameMain() {
        // --- Window Setup ---
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true); // Allow canvas to receive keyboard input
        requestFocus(); // Request focus immediately

        // --- Rendering Buffer Setup ---
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        // --- Core Component Initialization ---
        screen = new Screen(WIDTH, HEIGHT);
        keyboard = new Keyboard();
        addKeyListener(new GameKeyListener());

        // <<< Add Mouse Listeners >>>
        addMouseListener(new MouseInputAdapter()); // Add adapter for press/release/etc.
        addMouseMotionListener(this); // Add motion listener (implements interface)
        // -------------------------

        // --- Level Loading ---
        log.info("Loading level...");
        level = MapLoader.loadLevelFromImage("/assets/level/spawn_map.png", 16);
        log.info("Level loading complete.");

        // --- ECS and System Initialization ---
        log.info("Initializing ECS and Systems...");
        entityManager = new EntityManager();
        // Pass necessary dependencies to each system's constructor
        movementSystem = new MovementSystem(entityManager);
        animationSystem = new AnimationSystem(entityManager);
        renderSystem = new RenderSystem(entityManager, screen);
        cameraSystem = new CameraSystem(entityManager, screen, level);
        collisionSystem = new CollisionSystem(entityManager, level);
        playerInputSystem = new PlayerInputSystem(entityManager, keyboard);
        debugRenderSystem = new DebugRenderSystem(entityManager, screen, level);
        aiSystem = new AISystem(entityManager);
        combatSystem = new CombatSystem(entityManager);
        visualEffectsSystem = new VisualEffectsSystem(entityManager);
        lifetimeSystem = new LifetimeSystem(entityManager);
        particlePhysicsSystem = new ParticlePhysicsSystem(entityManager);
        experienceSystem = new ExperienceSystem(entityManager);             // <<< Instantiate ExperienceSystem
        statCalculationSystem = new StatCalculationSystem(entityManager);
        interactionSystem = new InteractionSystem(entityManager); // Instantiate InteractionSystem
        corpseDecaySystem = new CorpseDecaySystem(entityManager); // Instantiate CorpseDecaySystem
        uiManager = new UIManager(/* Pass entityManager if needed later */);
        log.info("Systems and UIManager initialized.");

        // --- Initial Entity Spawning ---
        spawnEntitiesFromMap();

        // Create camera entity (its logic might be simple or complex depending on CameraSystem)
        cameraEntity = createCamera();

        // --- Post-Spawn Checks ---
        // Verify that the player was actually spawned from the map data
        if (playerEntity == -1) {
            log.error("CRITICAL: Player entity was not created. No player spawn point found in map? Exiting.");
            // Handle this scenario - maybe throw exception or spawn default
            throw new RuntimeException("Failed to create player entity - No spawn point found.");
        } else {
            log.info("Player entity successfully created with ID: {}", playerEntity);
            // Calculate initial stats for player and any spawned entities
            log.info("Performing initial stat calculation...");
            statCalculationSystem.recalculateStats(playerEntity); // Calculate player stats
            // Calculate stats for all entities with StatsComponent initially
            for (int entityId : entityManager.getEntitiesWith(StatsComponent.class)) {
                if (entityId != playerEntity) { // Avoid double calculation if player already done
                    statCalculationSystem.recalculateStats(entityId);
                }
            }
            log.info("Initial stat calculation complete.");
        }
        // --- Setup UI Panels ---
        setupUILayout(); // Call helper method to create UI panels
        // ------------------------
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
        // Graphics
        String playerSheetPath = "/assets/sprites/player.png"; // TODO: Verify path
        BufferedImage[][] walkFrames = new BufferedImage[4][3]; // [Direction][Frame]
        boolean playerSpritesLoaded = false;
        int playerSpriteSize = 32; // TODO: Verify size
        int framesPerDirection = 3; // TODO: Verify frame count
        try {
            BufferedImage sheet = AssetLoader.loadImage(playerSheetPath);
            if (sheet != null && sheet.getWidth() >= playerSpriteSize * 4 && sheet.getHeight() >= playerSpriteSize * 3) {
                for (int frameRow = 0; frameRow < framesPerDirection; frameRow++) {
                    for (int dirCol = 0; dirCol < 4; dirCol++) {
                        walkFrames[dirCol][frameRow] = AssetLoader.getSprite(sheet, dirCol, frameRow, playerSpriteSize, playerSpriteSize);
                    }
                }
                playerSpritesLoaded = true;
            } else {
                if (sheet != null) { // Sheet loaded but was too small
                    log.error("Player spritesheet is too small (%dx%d) for expected layout (4x%d sprites of size %dx%d)%n",
                            sheet.getWidth(),
                            sheet.getHeight(),
                            framesPerDirection,
                            playerSpriteSize,
                            playerSpriteSize
                    );
                } // Error message for sheet == null is handled by loadImage
            }
        } catch (Exception e) {
            log.error("Error loading player spritesheet or extracting sprites: {}", e.getMessage(), e);
        }
        // Determine the initial sprite - use first down frame if loaded, otherwise get a placeholder
        BufferedImage initialSprite;
        if (playerSpritesLoaded && walkFrames[0] != null && walkFrames[0].length > 0 && walkFrames[0][0] != null && walkFrames[0][0].getWidth() > 1) {
            initialSprite = walkFrames[0][0];
        } else {
            log.warn("Using placeholder for initial player sprite.");
            initialSprite = AssetLoader.createPlaceholderImage(playerSpriteSize, playerSpriteSize); // Use placeholder creator
        }
        // Components
        entityManager.addComponent(entityId, new PositionComponent(spawnX, spawnY));
        entityManager.addComponent(entityId, new VelocityComponent(0, 0));
        entityManager.addComponent(entityId, new SpriteComponent(initialSprite));
        entityManager.addComponent(entityId, new AnimationComponent(walkFrames, 10));
        entityManager.addComponent(entityId, new PlayerComponent());
        entityManager.addComponent(entityId, new ColliderComponent(15, 14, 8, 15));
        entityManager.addComponent(entityId, new HealthComponent(100));
        entityManager.addComponent(entityId, new NameComponent("Lolzords"));
        entityManager.addComponent(entityId, new AttackComponent(10f, 45f, 0.5f)); // Dmg=10, Range=45px, Cooldown=0.5s
        entityManager.addComponent(entityId, new ExperienceComponent()); // <<< Add Experience Component
        entityManager.addComponent(entityId, new StatsComponent()); // <<< Add Stats Component (with defaults)
        log.info("Player Entity Created with ID: {}", entityId);
        return entityId;
    }

    private int createVillagerNPC(int spawnX, int spawnY) {
        log.debug("Creating Villager NPC at: ({}, {})", spawnX, spawnY);
        int entityId = entityManager.createEntity();
        // --- Graphics ---
        String sheetPath = "/assets/sprites/blondLong.png";
        int spriteSize = 32;
        int framesPerDir = 3;
        BufferedImage[][] walkFrames = new BufferedImage[4][];
        BufferedImage initialSprite = null;
        try {
            List<BufferedImage> downFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 0, framesPerDir, spriteSize, spriteSize, true);
            List<BufferedImage> leftFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 1, framesPerDir, spriteSize, spriteSize, true);
            List<BufferedImage> rightFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 2, framesPerDir, spriteSize, spriteSize, true);
            List<BufferedImage> upFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 3, framesPerDir, spriteSize, spriteSize, true);

            if (!downFrames.isEmpty()) {
                walkFrames[LEFT] = leftFrames.toArray(new BufferedImage[0]);
                walkFrames[DOWN] = downFrames.toArray(new BufferedImage[0]);
                walkFrames[RIGHT] = rightFrames.toArray(new BufferedImage[0]);
                walkFrames[UP] = upFrames.toArray(new BufferedImage[0]);
                initialSprite = walkFrames[0][0];
            }
        } catch (Exception e) {
            log.error("Error loading villager sprites", e);
        }
        if (initialSprite == null) {
            initialSprite = AssetLoader.createPlaceholderImage(spriteSize, spriteSize);
        }
        // --- Core Components ---
        entityManager.addComponent(entityId, new PositionComponent(spawnX, spawnY));
        entityManager.addComponent(entityId, new VelocityComponent(0, 0));
        entityManager.addComponent(entityId, new SpriteComponent(initialSprite));
        entityManager.addComponent(entityId, new AnimationComponent(walkFrames, 15));
        entityManager.addComponent(entityId, new ColliderComponent(20, 28, 6, 4));
        // --- Gameplay Components ---
        entityManager.addComponent(entityId, new HealthComponent(100));
        entityManager.addComponent(entityId, new NPCComponent());
        entityManager.addComponent(entityId, new InteractableComponent(40f));
        entityManager.addComponent(entityId, new NameComponent("Graze"));
        entityManager.addComponent(entityId, new WanderAIComponent(spawnX, spawnY, 64f, 3.0f, 8.0f, 0.8f, 100f)); // Added aggroRadius=100
        log.debug("Villager NPC Entity Created with ID: {}", entityId);
        return entityId;
    }

    private int createEnemyDeidara(int spawnX, int spawnY) {
        log.debug("Creating Enemy at: ({}, {})", spawnX, spawnY);
        int entityId = entityManager.createEntity();
        // --- Graphics ---
        String sheetPath = "/assets/sprites/deidara.png";
        int spriteSize = 32;
        int framesPerDir = 3;
        BufferedImage[][] walkFrames = new BufferedImage[4][framesPerDir]; // Reuse structure? Or maybe slime only hops (1 anim)?
        BufferedImage initialSprite = null;
        try {
            List<BufferedImage> downFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 0, framesPerDir, spriteSize, spriteSize, true);
            List<BufferedImage> leftFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 1, framesPerDir, spriteSize, spriteSize, true);
            List<BufferedImage> rightFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 2, framesPerDir, spriteSize, spriteSize, true);
            List<BufferedImage> upFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 3, framesPerDir, spriteSize, spriteSize, true);

            if (!downFrames.isEmpty()) {
                walkFrames[LEFT] = leftFrames.toArray(new BufferedImage[0]);
                walkFrames[DOWN] = downFrames.toArray(new BufferedImage[0]);
                walkFrames[RIGHT] = rightFrames.toArray(new BufferedImage[0]);
                walkFrames[UP] = upFrames.toArray(new BufferedImage[0]);
                initialSprite = walkFrames[0][0];
            }
        } catch (Exception e) {
            log.error("Error loading slime sprites: ", e.getMessage());
        }
        if (initialSprite == null) {
            initialSprite = AssetLoader.createPlaceholderImage(spriteSize, spriteSize);
        }
        // --- Components ---
        entityManager.addComponent(entityId, new PositionComponent(spawnX, spawnY));
        entityManager.addComponent(entityId, new VelocityComponent(0, 0));
        entityManager.addComponent(entityId, new SpriteComponent(initialSprite));
        entityManager.addComponent(entityId, new AnimationComponent(walkFrames, 20));
        entityManager.addComponent(entityId, new ColliderComponent(24, 16, 4, 16));
        entityManager.addComponent(entityId, new HealthComponent(30));
        entityManager.addComponent(entityId, new EnemyComponent());
        entityManager.addComponent(entityId, new NameComponent("Deidara"));
        entityManager.addComponent(entityId, new AttackComponent(5f, 35f, 1.5f)); // Dmg=5, Range=35px, Cooldown=1.5s
        entityManager.addComponent(entityId, new WanderAIComponent(spawnX, spawnY, 48f, 1.0f, 4.0f, 0.6f, 120f)); // Added aggroRadius=120
        entityManager.addComponent(entityId, new XPValueComponent(15)); // XP value
        entityManager.addComponent(entityId, new ParticipantComponent()); // Tracker for XP sharing
        entityManager.addComponent(entityId, new StatsComponent(5, 8, 2, 5));
        log.debug("Slime Enemy Entity Created with ID: {}", entityId);
        return entityId;
    }

    private int createCamera() {
        cameraEntity = entityManager.createEntity();
        entityManager.addComponent(cameraEntity, new CameraComponent(0, 0));
        log.debug("Camera Entity Created with ID: {}", cameraEntity);
        return cameraEntity;
    }

    /** Helper method to create and add the main UI panels */
    private void setupUILayout() {
        log.info("Setting up UI layout...");

        // --- Define Layout Dimensions (Adjust these values) ---
        int sidebarWidth = 180; // Width of the right sidebar
        int margin = 5;         // Small margin between elements
        int contentWidth = sidebarWidth - (margin * 2);

        // --- Sidebar Panel (Right) ---
        UIPanel sidebarPanel = new UIPanel(WIDTH - PANEL_WIDTH, 0)
                .setSize(sidebarWidth, HEIGHT)
                .setBackgroundColor(new Color(40, 40, 40, 230)); // Dark semi-transparent gray
        uiManager.addPanel(sidebarPanel);

        // --- Chat Panel (Bottom Left) ---
        Vector2i chatSize = new Vector2i(WIDTH - PANEL_WIDTH, CHAT_HEIGHT);
        UIPanel chatPanel = new UIPanel(0, HEIGHT - CHAT_HEIGHT)
                .setSize(WIDTH - PANEL_WIDTH, CHAT_HEIGHT)
                .setBackgroundColor(new Color(60, 60, 60, 200)); // Slightly lighter gray
        uiManager.addPanel(chatPanel);

        // --- Panels *within* the Sidebar ---
        // Note: Positions for these child panels are RELATIVE to the sidebarPanel's position (0,0) initially.
        // The UIPanel's update/render logic handles the absolute positioning.

        // Minimap Panel (Top Right of Sidebar)
        int minimapSize = PANEL_WIDTH - (margin * 2); // Square minimap, with margins
        Vector2i minimapPos = new Vector2i(margin, margin); // Relative to sidebar top-left
        Vector2i minimapDim = new Vector2i(contentWidth, contentWidth);
        UIPanel minimapPanel = new UIPanel(minimapPos, minimapDim);
        minimapPanel.setBackgroundColor(Color.BLACK); // Black background for minimap
        sidebarPanel.addComponent(minimapPanel); // Add minimap *to* the sidebar

        // Stats/Equipment Panel (Below Minimap)
        int statsPanelY = minimapPos.y + minimapDim.y + margin;
        int statsPanelHeight = 100; // Example height
        Vector2i statsPos = new Vector2i(margin, statsPanelY);
        Vector2i statsSize = new Vector2i(contentWidth, statsPanelHeight);
        // Create the specific panel, passing EntityManager and player ID
        PlayerStatsPanel statsPanel = new PlayerStatsPanel(statsPos, statsSize, entityManager, playerEntity);
        // No need to set color here, PlayerStatsPanel constructor does it
        // No need to manually add labels here, PlayerStatsPanel constructor does it
        sidebarPanel.addComponent(statsPanel); // Add the specialized panel to the sidebar
        // <<< End Stats Panel Change >>>

        // Button Bar Panel
        int buttonPanelY = statsPos.y + statsSize.y + margin;
        int buttonPanelHeight = 40;
        Vector2i buttonPos = new Vector2i(margin, buttonPanelY);
        Vector2i buttonSize = new Vector2i(contentWidth, buttonPanelHeight);
        UIPanel buttonPanel = new UIPanel(buttonPos, buttonSize);
        buttonPanel.setBackgroundColor(new Color(90, 70, 70, 210));
        sidebarPanel.addComponent(buttonPanel);

        // --- Add Example Button to Button Panel ---
        Vector2i btnSize = new Vector2i(40, 10);
        int btnY = (buttonPanelHeight - btnSize.y); // Center vertically within button panel
        Vector2i btn1Pos = new Vector2i(2, 2); // Add margin from panel edge
        UIButton skillsButton = new UIButton(btn1Pos, btnSize, "Skills", () -> log.info("Skills Button Clicked!"))
                .setFont(new Font("Arial", Font.PLAIN, 8))
                .setBackgroundColor(Color.WHITE)
                .setLabelColor(Color.BLACK);
        buttonPanel.addComponent(skillsButton); // Add button TO the panel

        Vector2i btn2Pos = new Vector2i(btn1Pos.x + btnSize.x + 2, 2);
        UIButton optionsButton = new UIButton(btn2Pos, btnSize, "Options", () -> log.info("Options Button Clicked!"))
                .setBackgroundColor(Color.WHITE)
                .setFont(new Font("Arial", Font.PLAIN, 8))
                .setLabelColor(Color.BLACK);
        buttonPanel.addComponent(optionsButton);
        // ------------------------------------------


        // Inventory Panels (Placeholders)
        int invPanelY = buttonPos.y + buttonSize.y + margin;
        int invPanelHeight = 80;
        Vector2i invSize = new Vector2i(contentWidth, invPanelHeight);
        UIPanel inventoryPanel1 = new UIPanel(new Vector2i(margin, invPanelY), invSize);
        inventoryPanel1.setBackgroundColor(new Color(70, 90, 70, 210));
        sidebarPanel.addComponent(inventoryPanel1);

        // --- Add Components to Chat Panel ---
        int inputHeight = 25; // Height of the text input field
        int areaHeight = chatSize.y - inputHeight - margin; // Height for text display area

        // Text Area (Displays messages)
        Vector2i areaPos = new Vector2i(margin, margin); // Relative to chatPanel
        Vector2i areaSize = new Vector2i(chatSize.x - (margin * 2), areaHeight);
        chatArea = new UIChatDisplay(areaPos, areaSize).setMaxLines(15); // Show more lines
        chatPanel.addComponent(chatArea);

        // Text Input (Where player types)
        Vector2i inputPos = new Vector2i(margin, areaPos.y + areaHeight + margin); // Below text area
        Vector2i inputSize = new Vector2i(chatSize.x - (margin * 2), inputHeight);
        chatInput = new UITextInput(inputPos, inputSize).setBackgroundColor(Color.LIGHT_GRAY); // Different background
        chatPanel.addComponent(chatInput);
        // ----------------------------------

        log.info("UI layout panels and components created.");
    }

    // --- Game Loop and Core Methods ---

    /** Starts the game thread. */
    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
        log.info("Game thread started.");
    }

    /** Stops the game thread gracefully. */
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
        keyboard.update();

        // --- Update Systems in Order ---
        playerInputSystem.update(deltaTime);
        aiSystem.update(deltaTime); // AI now handles attacks
        interactionSystem.update(deltaTime);
        combatSystem.update(deltaTime);
        experienceSystem.update(deltaTime);      // <<< Process DefeatedComponent, grant XP, add LevelUpEventComponent
        statCalculationSystem.update(deltaTime); // <<< Process LevelUpEventComponent, recalculate stats
        particlePhysicsSystem.update(deltaTime);
        collisionSystem.update(deltaTime);
        movementSystem.update(deltaTime);
        lifetimeSystem.update(deltaTime);    // Update particle lifetimes, destroy expired entities
        visualEffectsSystem.update(deltaTime); // Update flash timers for TookDamageComponent
        corpseDecaySystem.update(deltaTime);
        cameraSystem.update(deltaTime);
        animationSystem.update(deltaTime);

        // --- Update UI ---
        uiManager.update(inputState, deltaTime); // Pass input state here later if input is refactored
        // -----------------

        // --- Handle Chat Submission ---
        if (chatInput != null && chatInput.isFocused()) {
            String submitted = chatInput.getSubmittedTextAndClear();
            if (submitted != null) {
                log.info("Chat Submitted: {}", submitted);
                // Add message to chat area
                // TODO: Add player name prefix?
                chatArea.addLine(submitted);
                // TODO: Send message to server / process chat command
                // TODO: Implement different say types (scream, whisper) based on prefix?
                chatInput.setFocus(false); // Lose focus after submitting
            }
        }
        // ---------------------------

        // --- Clear Per-Frame Input Events ---
        // Do this LAST in the update cycle
        inputState.clearFrameEvents();
        // ----------------------------------
    }

    /** Renders the current game state. */
    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            try {
                createBufferStrategy(3); // Use triple buffering
            } catch (IllegalStateException e) {
                log.error("Error creating BufferStrategy", e);
                // Handle error - maybe stop game loop?
                running = false;
                return;
            }
            return; // Exit render call for this frame, will try again next frame
        }

        // Prepare screen for drawing
        screen.clear(); // Clear the pixel buffer

        // Camera offset is set internally by CameraSystem.update() called in update()

        // Render Tiles
        level.render(screen);

        // Render Entities (Players, NPCs, Mobs, Items)
        renderSystem.render();

        // Render Debug Info
        debugRenderSystem.render();

        // --- Draw buffer to screen ---
        System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);
        // Graphics g = bs.getDrawGraphics();
        // g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null); // Draw scaled image
        // g.dispose(); // Release graphics context
        // bs.show(); // Show the next available buffer

        // 4. Get Graphics context for the final image buffer
        Graphics g = null;
        try {
            g = image.getGraphics(); // Get graphics for the image we draw to the canvas
            if (g != null) {
                // --- Render UI Layer ---
                // Draw UI elements directly onto the image, on top of the game world
                uiManager.render(g);
                // ---------------------
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

    public static void main(String[] args) {
        GameMain game = new GameMain();
        game.frame = new JFrame(TITLE);
        game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.frame.setResizable(false);
        game.frame.add(game);
        game.frame.pack();
        game.frame.setLocationRelativeTo(null);
        game.frame.setVisible(true);
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
            if (chatInput != null && e.getButton() == MouseEvent.BUTTON1) { // Only on left click
                Vector2i inputAbsPos = chatInput.getAbsolutePosition();
                Rectangle inputBounds = new Rectangle(inputAbsPos.x, inputAbsPos.y, chatInput.size.x, chatInput.size.y);
                if (inputBounds.contains(logicalX, logicalY)) {
                    if (!chatInput.isFocused()) {
                        log.debug("Giving focus to chat input via mouse click.");
                        chatInput.setFocus(true);
                    }
                    // Optional: Consume event if click was on UI?
                    // e.consume();
                } else {
                    // Click was outside the chat input
                    if (chatInput.isFocused()) {
                        log.debug("Removing focus from chat input via mouse click outside.");
                        chatInput.setFocus(false);
                    }
                }
            }
            // -----------------------------------------
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

            // --- Handle Chat Input Focus (Ctrl+Enter) ---
            if (e.isControlDown() && keyCode == KeyEvent.VK_ENTER) {
                if (chatInput != null && !chatInput.isFocused()) {
                    log.debug("Giving focus to chat input via Ctrl+Enter.");
                    chatInput.setFocus(true);
                    e.consume(); // Consume the combo
                    return;
                }
            }

            // --- Handle Chat Input Focus (Enter to Submit/Lose Focus) ---
            // <<< REMOVED: Enter no longer gives focus, only submits >>>
            if (keyCode == KeyEvent.VK_ENTER) {
                if (chatInput != null && chatInput.isFocused()) {
                    // Let chatInput handle Enter for submission (logic moved to update loop)
                    chatInput.handleKeyPress(e);
                    // Focus loss happens in update loop after text is processed
                    e.consume(); // Consume Enter so game doesn't react
                    return;
                }
                // <<< REMOVED else block that gave focus on Enter >>>
            }

            // --- Pass key press to focused UI element ---
            if (chatInput != null && chatInput.isFocused()) {
                chatInput.handleKeyPress(e);
                e.consume(); // Consume typing keys
                return; // Stop processing here if chat has focus
            }

            // --- If chat doesn't have focus, update Keyboard state for game systems ---
            keyboard.keyPressed(e); // Update the holder object
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // If chat doesn't have focus, update Keyboard state
            if (chatInput == null || !chatInput.isFocused()) {
                keyboard.keyReleased(e);
            }
            // Note: No need to pass release events to chatInput usually
        }
    }
    // --- End Input Listener Inner Classes ---
}