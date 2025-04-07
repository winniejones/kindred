package com.kindred;

import com.kindred.engine.entity.components.*;
import com.kindred.engine.entity.core.EntityManager;
import com.kindred.engine.entity.systems.*;
import com.kindred.engine.input.Keyboard;
import com.kindred.engine.level.Level;
import com.kindred.engine.level.MapLoader;
import com.kindred.engine.render.Screen;
import com.kindred.engine.resource.AssetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

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
    private final MovementSystem movementSystem;
    private final AnimationSystem animationSystem;

    private final RenderSystem renderSystem;
    private final CameraSystem cameraSystem;
    private final CollisionSystem collisionSystem;
    private final PlayerInputSystem playerInputSystem;
    private final DebugRenderSystem debugRenderSystem;

    private int cameraEntity;

    private final Level level;

    public GameMain() {
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true);
        requestFocus();

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        screen = new Screen(WIDTH, HEIGHT);
        keyboard = new Keyboard();
        // Create level
        level = MapLoader.loadLevelFromImage("/assets/level/spawn_map.png", 16);

        addKeyListener(keyboard);

        entityManager = new EntityManager();
        movementSystem = new MovementSystem(entityManager);
        animationSystem = new AnimationSystem(entityManager);
        renderSystem = new RenderSystem(entityManager, screen);
        cameraSystem = new CameraSystem(entityManager, screen, level);
        collisionSystem = new CollisionSystem(entityManager, level);
        playerInputSystem = new PlayerInputSystem(entityManager, keyboard);
        debugRenderSystem = new DebugRenderSystem(entityManager, screen, level);

        // Create player entity with position and velocity
        int playerEntity = createPlayer();
        cameraEntity = createCamera();

    }


    private int createPlayer() {
        String playerSheetPath = "/assets/sprites/player.png";
        BufferedImage[][] walkFrames = new BufferedImage[4][3]; // 4 directions, 3 frames each
        boolean playerSpritesLoaded = false;
        int playerSpriteSize = 32;
        System.out.println("Attempting to load player animation frames using nested loops...");
        try {
            BufferedImage sheet = AssetLoader.loadImage(playerSheetPath);

            if (sheet != null && sheet.getWidth() >= playerSpriteSize * 4 && sheet.getHeight() >= playerSpriteSize * 3) {
                // Nested loops based on user's provided logic
                // Assumes sheet layout: Columns are directions (0-3), Rows are frames (0-2)
                for (int frameRow = 0; frameRow < 3; frameRow++) {        // Iterate through frames (rows on sheet)
                    for (int dirCol = 0; dirCol < 4; dirCol++) {      // Iterate through directions (columns on sheet)
                        // Get sprite using AssetLoader
                        BufferedImage sprite = AssetLoader.getSprite(sheet, dirCol, frameRow, playerSpriteSize, playerSpriteSize);
                        // Assign to walkFrames[direction][frame]
                        walkFrames[dirCol][frameRow] = sprite;
                        // Check if sprite loading failed (AssetLoader returns placeholder)
                        if (sprite == null || sprite.getWidth() <= 1) {
                            System.err.printf("Warning: Failed to load player sprite at sheet coords (col:%d, row:%d)%n", dirCol, frameRow);
                        }
                    }
                }
                playerSpritesLoaded = true; // Assume loaded if sheet was valid, even if some getSprite calls failed (they return placeholders)
                System.out.println("Player animation frames processed.");
                // Optional: Add check here to see if any frame is actually null if needed
            } else {
                if (sheet == null) {
                    System.err.println("Failed to load player spritesheet: " + playerSheetPath);
                } else {
                    System.err.printf("Player spritesheet is too small (%dx%d) for expected layout (4x%d sprites of size %dx%d)%n",
                            sheet.getWidth(), sheet.getHeight(), 3, playerSpriteSize, playerSpriteSize);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading player spritesheet or extracting sprites: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for more detail
        }
        // Determine the initial sprite - use first down frame if loaded, otherwise get a placeholder
        BufferedImage initialSprite;
        if (playerSpritesLoaded && walkFrames[0][0] != null && walkFrames[0][0].getWidth() > 1) {
            initialSprite = walkFrames[0][0]; // Default to first down frame
        } else {
            // If loading failed, get a placeholder from AssetLoader (which creates one internally)
            // We try loading the sheet again, loadImage returns placeholder on failure.
            // Alternatively, create a dedicated placeholder method in AssetLoader if preferred.
            System.err.println("Using placeholder for initial player sprite.");
            initialSprite = AssetLoader.loadImage(playerSheetPath); // Will return placeholder if path failed before
            // Ensure the placeholder isn't null itself (loadImage might return null on severe error)
            if (initialSprite == null || initialSprite.getWidth() <= 1) {
                initialSprite = new BufferedImage(playerSpriteSize, playerSpriteSize, BufferedImage.TYPE_INT_ARGB); // Ultimate fallback: transparent square
            }
        }

        int playerEntity = entityManager.createEntity();

        // --- Get Player Start Position from Level ---
        int startX, startY;
        int loadedSpawnX = level.getPlayerSpawnX();
        int loadedSpawnY = level.getPlayerSpawnY();

        if (loadedSpawnX != -1 && loadedSpawnY != -1) {
            // Use spawn point found in the map file
            startX = loadedSpawnX;
            startY = loadedSpawnY;
            System.out.println("Using spawn point from map: (" + startX + ", " + startY + ")");
        } else {
            // Fallback if no spawn point color was found in the map
            startX = 100; // Default X (ensure this is a walkable area!)
            startY = 100; // Default Y (ensure this is a walkable area!)
            System.out.println("Warning: No spawn point found in map file. Using default spawn: (" + startX + ", " + startY + ")");
            // Optionally check if default is valid
            // if (level.isSolid(startX / level.getTileSize(), startY / level.getTileSize())) {
            //     System.err.println("FATAL: Default spawn point is inside a solid tile!");
            //     // Handle error - maybe search for a valid spot?
            // }
        }
        // --- End of spawn position logic ---

        System.out.println("Spawning player at: (" + startX + ", " + startY + ")");
        entityManager.addComponent(playerEntity, new PositionComponent(startX, startY));
        // --- End of change ---

        entityManager.addComponent(playerEntity, new VelocityComponent(0, 0));
        entityManager.addComponent(playerEntity, new SpriteComponent(initialSprite)); // Use loaded or default sprite
        // Ensure AnimationComponent constructor handles the structure of walkFrames correctly
        entityManager.addComponent(playerEntity, new AnimationComponent(walkFrames, 10)); // Use loaded frames, adjust frame delay (e.g., 10 updates per frame)
        entityManager.addComponent(playerEntity, new PlayerComponent());
        // Collider: 20 wide, 28 high, offset 6px right, 0px down from PositionComponent's x,y
        entityManager.addComponent(playerEntity, new ColliderComponent(15, 14, 8, 15));

        return playerEntity;
    }

    private int createCamera() {
        cameraEntity = entityManager.createEntity();
        entityManager.addComponent(cameraEntity, new CameraComponent(0, 0));// Initial camera offset/position
        // Add other camera components if needed (e.g., FollowTargetComponent)
        System.out.println("Camera Entity Created with ID: " + cameraEntity); // Debug output
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
            e.printStackTrace(); // Log interruption error
            Thread.currentThread().interrupt(); // Re-interrupt the current thread
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerUpdate = 1_000_000_000.0 / 60;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0, updates = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            boolean shouldRender = false; // Only render if an update happened

            // Process updates based on elapsed time
            while (delta >= 1) {
                update();
                updates++;
                delta--;
                shouldRender = true; // Mark that we need to render
            }

            // Render if an update occurred in this loop iteration
            // This decouples rendering from update rate slightly
            if (shouldRender) {
                render();
                frames++;
            } else {
                // Optional: Yield if no update/render to prevent busy-waiting
                try {
                    Thread.sleep(1); // Sleep briefly
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (System.currentTimeMillis() - timer >= 1000) {
                if (frame != null) { // Ensure frame exists
                    frame.setTitle(TITLE + " | UPS: " + updates + ", FPS: " + frames);
                }
                updates = 0;
                frames = 0;
                timer += 1000;
            }
        }
        // stop();
    }

    private void update() {
        keyboard.update();


        // --- Update Systems in Order ---
        playerInputSystem.update();
        collisionSystem.update();
        movementSystem.update();
        cameraSystem.update(); // Camera follows the new position
        animationSystem.update(); // Animation updates based on final velocity/state
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy(); // Get the buffer strategy for drawing
        if (bs == null) {
            createBufferStrategy(3); // Create triple buffering if it doesn't exist
            return; // Exit render call for this frame
        }

        // Prepare screen for drawing
        screen.clear(); // Clear the pixel buffer (e.g., fill with black or background color)

        // Render the level tiles (relative to camera)
        level.render(screen); // This needs to account for camera offset

        // Render entities (relative to camera)
        renderSystem.render(); // RenderSystem should handle camera offset

        debugRenderSystem.render();


        // Copy the rendered pixels from the Screen object to the Canvas's image buffer
        System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);

        // Draw the final image to the screen using the buffer strategy
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