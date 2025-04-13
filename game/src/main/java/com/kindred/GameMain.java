package com.kindred;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.systems.*;
import com.kindred.engine.input.Keyboard;
import com.kindred.engine.level.Level;
import com.kindred.engine.level.MapLoader;
import com.kindred.engine.level.SpawnPoint;
import com.kindred.engine.render.Screen;
import com.kindred.engine.resource.AssetLoader;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;
import java.util.Arrays;

@Slf4j
public class GameMain extends Canvas implements Runnable {

    private JFrame frame;
    private Thread gameThread;
    private boolean running = false;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int SCALE = 2;
    public static final String TITLE = "Kindred";

    private final BufferedImage image;
    private final int[] pixels;

    private final Screen screen;
    private final Keyboard keyboard;
    private final EntityManager entityManager;
    private final Level level;

    // Systems
    private final MovementSystem movementSystem;
    private final AnimationSystem animationSystem;
    private final RenderSystem renderSystem;
    private final CameraSystem cameraSystem;
    private final CollisionSystem collisionSystem;
    private final PlayerInputSystem playerInputSystem;
    private final DebugRenderSystem debugRenderSystem;
    private final AISystem aiSystem;
    private final CombatSystem combatSystem; // <<< Added CombatSystem instance

    // Entity IDs
    private int playerEntity = -1; // Initialize player entity ID to invalid (-1 indicates not spawned yet)
    private int cameraEntity;      // ID for the camera entity (if used)

    public GameMain() {
        // --- Window Setup ---
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true);
        requestFocus();

        // --- Rendering Buffer Setup ---
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        // --- Core Component Initialization ---
        screen = new Screen(WIDTH, HEIGHT);
        keyboard = new Keyboard();
        addKeyListener(keyboard);

        // --- Level Loading ---
        log.info("Loading level...");
        level = MapLoader.loadLevelFromImage("/assets/level/spawn_map.png", 16);
        log.info("Level loading complete.");

        // --- ECS and System Initialization ---
        log.info("Initializing ECS and Systems...");
        entityManager = new EntityManager();
        movementSystem = new MovementSystem(entityManager);
        animationSystem = new AnimationSystem(entityManager);
        renderSystem = new RenderSystem(entityManager, screen);
        cameraSystem = new CameraSystem(entityManager, screen, level);
        collisionSystem = new CollisionSystem(entityManager, level);
        playerInputSystem = new PlayerInputSystem(entityManager, keyboard);
        debugRenderSystem = new DebugRenderSystem(entityManager, screen, level);
        aiSystem = new AISystem(entityManager);
        combatSystem = new CombatSystem(entityManager);
        log.info("Systems initialized.");

        // --- Initial Entity Spawning ---
        spawnEntitiesFromMap();

        // Create camera entity
        cameraEntity = createCamera();

        // --- Post-Spawn Checks ---
        if (playerEntity == -1) {
             log.error("CRITICAL: Player entity was not created. No player spawn point found in map? Exiting.");
             throw new RuntimeException("Failed to create player entity - No spawn point found.");
        } else {
             log.info("Player entity successfully created with ID: {}", playerEntity);
        }
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
                case ENEMY_SLIME:
                    createBlondLong(spawnX, spawnY);
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
        // --- Graphics ---
        String playerSheetPath = "/assets/sprites/player.png";
        BufferedImage[][] walkFrames = new BufferedImage[4][3]; // 4 directions, 3 frames each
        boolean playerSpritesLoaded = false;
        int playerSpriteSize = 32;
        int framesPerDirection = 3;
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
                            sheet.getWidth(), sheet.getHeight(), framesPerDirection, playerSpriteSize, playerSpriteSize);
                } // Error message for sheet == null is handled by loadImage
            }
        } catch (Exception e) {
            log.error("Error loading player spritesheet or extracting sprites: {}", e.getMessage(), e);
        }
        // Determine the initial sprite - use first down frame if loaded, otherwise get a placeholder
        BufferedImage initialSprite;
        if (playerSpritesLoaded && walkFrames[0][0] != null && walkFrames[0][0].getWidth() > 1) {
            initialSprite = walkFrames[0][0];
        } else {
            log.error("Using placeholder for initial player sprite.");
            initialSprite = AssetLoader.createPlaceholderImage(playerSpriteSize, playerSpriteSize); // Use placeholder creator
        }

        entityManager.addComponent(entityId, new PositionComponent(spawnX, spawnY));
        entityManager.addComponent(entityId, new VelocityComponent(0, 0));
        entityManager.addComponent(entityId, new SpriteComponent(initialSprite));
        entityManager.addComponent(entityId, new AnimationComponent(walkFrames, 10));
        entityManager.addComponent(entityId, new PlayerComponent());
        entityManager.addComponent(entityId, new ColliderComponent(15, 14, 8, 15));
        entityManager.addComponent(entityId, new HealthComponent(100,100));
        entityManager.addComponent(entityId, new AttackComponent(10f, 45f, 0.5f)); // Dmg=10, Range=45px, Cooldown=0.5s
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

            if (!downFrames.isEmpty()) { // Basic check if loading worked
                walkFrames[0] = downFrames.toArray(new BufferedImage[0]);
                walkFrames[1] = leftFrames.toArray(new BufferedImage[0]);
                walkFrames[2] = rightFrames.toArray(new BufferedImage[0]);
                walkFrames[3] = upFrames.toArray(new BufferedImage[0]);
                initialSprite = walkFrames[0][0]; // Use first down frame
            }
        } catch (Exception e) {
            log.error("Error loading villager sprites: " + e.getMessage());
        }
        if (initialSprite == null) {
            initialSprite = AssetLoader.loadImage(sheetPath);
            if (initialSprite == null || initialSprite.getWidth() <= 1)
                initialSprite = new BufferedImage(spriteSize, spriteSize, BufferedImage.TYPE_INT_ARGB); // Ultimate fallback
        }
        // --- Core Components ---
        entityManager.addComponent(entityId, new PositionComponent(spawnX, spawnY));
        entityManager.addComponent(entityId, new VelocityComponent(0, 0));
        entityManager.addComponent(entityId, new SpriteComponent(initialSprite));
        entityManager.addComponent(entityId, new AnimationComponent(walkFrames, 15));
        entityManager.addComponent(entityId, new ColliderComponent(20, 28, 6, 4));
        // --- Gameplay Components ---
        entityManager.addComponent(entityId, new HealthComponent(100, 100));
        entityManager.addComponent(entityId, new NPCComponent());
        // Add aggro radius to WanderAIComponent
        entityManager.addComponent(entityId, new WanderAIComponent(spawnX, spawnY, 64f, 3.0f, 8.0f, 0.8f, 100f)); // Added aggroRadius=100
        log.debug("Villager NPC Entity Created with ID: {}", entityId);
        return entityId;
    }

    private int createBlondLong(int spawnX, int spawnY) {
        log.debug("Creating Enemy at: ({}, {})", spawnX, spawnY);
        int entityId = entityManager.createEntity();
        // --- Graphics ---
        String sheetPath = "/assets/sprites/blondLong.png";
        int spriteSize = 32;
        int framesPerDir = 2;
        BufferedImage[][] walkFrames = new BufferedImage[4][]; // Reuse structure? Or maybe slime only hops (1 anim)?
        BufferedImage initialSprite = null;
        try {
            List<BufferedImage> hopFrames = AssetLoader.loadAnimationFrames(sheetPath, 0, 0, framesPerDir, spriteSize, spriteSize, true);
            if (!hopFrames.isEmpty()) {
                walkFrames[0] = hopFrames.toArray(new BufferedImage[0]);
                walkFrames[1] = walkFrames[0];
                walkFrames[2] = walkFrames[0];
                walkFrames[3] = walkFrames[0];
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
        entityManager.addComponent(entityId, new HealthComponent(30, 30));
        entityManager.addComponent(entityId, new EnemyComponent());
        // <<< Add AttackComponent to Slime >>>
        entityManager.addComponent(entityId, new AttackComponent(5f, 35f, 1.5f)); // Dmg=5, Range=35px, Cooldown=1.5s
        // <<< Add aggro radius to WanderAIComponent >>>
        entityManager.addComponent(entityId, new WanderAIComponent(spawnX, spawnY, 48f, 1.0f, 4.0f, 0.6f, 120f)); // Added aggroRadius=120

        log.debug("Slime Enemy Entity Created with ID: {}", entityId);
        return entityId;
    }

    /**
     * Creates the Camera entity (currently just holds CameraComponent).
     *
     * @return The entity ID of the created camera.
     */
    private int createCamera() {
        cameraEntity = entityManager.createEntity();
        entityManager.addComponent(cameraEntity, new CameraComponent(0, 0));
        log.debug("Camera Entity Created with ID: {}", cameraEntity);
        return cameraEntity;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            gameThread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e); // Log interruption error
            Thread.currentThread().interrupt(); // Re-interrupt the current thread
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerUpdate = 1_000_000_000.0 / 60.0; // Target 60 updates per second
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0, updates = 0;

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            lastTime = now;
            delta += elapsed / nsPerUpdate;

            boolean shouldRender = false; // Only render if an update happened

            // Process updates based on elapsed time
            while (delta >= 1) {
                // Calculate deltaTime in seconds for this update tick
                float deltaTime = (float) nsPerUpdate / 1_000_000_000.0f;
                update(deltaTime); // Pass delta time to update logic
                updates++;
                delta--;
                shouldRender = true; // Render after updates
            }

            // Render if an update occurred (or based on separate rendering timer if needed)
            if (shouldRender) {
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

            if (System.currentTimeMillis() - timer >= 1000) {
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

    private void update(float deltaTime) {
        keyboard.update();
        // --- Update Systems in Order ---
        playerInputSystem.update(deltaTime);
        aiSystem.update(deltaTime); // AI now handles attacks
        combatSystem.update(deltaTime);
        collisionSystem.update(deltaTime);
        movementSystem.update(deltaTime);
        cameraSystem.update(deltaTime);
        animationSystem.update(deltaTime);
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) { createBufferStrategy(3); return; }
        screen.clear();

        // Camera offset is now set internally by CameraSystem.update()

        // Render Tiles
        level.render(screen);

        // Render Entities (Players, NPCs, Mobs, Items)
        renderSystem.render();

        // Render Debug Info
        debugRenderSystem.render();

        // --- Draw buffer to screen ---
        System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);
        Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null); // Draw scaled image
        g.dispose(); // Release graphics context
        bs.show(); // Show the next available buffer
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
}