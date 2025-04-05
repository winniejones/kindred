package com.kindred;

import com.kindred.engine.entity.*;
import com.kindred.engine.input.Keyboard;
import com.kindred.engine.level.Level;
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

    private final int playerEntity;
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
        level = new Level(50, 30, 32);
        level.generateTestMap();
        addLevelBoundaries();

        addKeyListener(keyboard);

        entityManager = new EntityManager();
        movementSystem = new MovementSystem(entityManager);
        animationSystem = new AnimationSystem(entityManager);
        renderSystem = new RenderSystem(entityManager, screen);
        cameraSystem = new CameraSystem(entityManager, screen);
        collisionSystem = new CollisionSystem(entityManager, level);
        playerInputSystem = new PlayerInputSystem(entityManager, keyboard);
        debugRenderSystem = new DebugRenderSystem(entityManager, screen, level);

        // Create player entity with position and velocity
        playerEntity = createPlayer();
        cameraEntity = createCamera();

        // Add solid walls to the level borders
    }

    // Helper to add solid boundaries to the level
    private void addLevelBoundaries() {
        for (int x = 0; x < level.getWidth(); x++) {
            level.setTile(x, 0, 0x222222, true); // Top wall
            level.setTile(x, level.getHeight() - 1, 0x222222, true); // Bottom wall
        }
        for (int y = 1; y < level.getHeight() - 1; y++) { // Avoid corners done above
            level.setTile(0, y, 0x222222, true); // Left wall
            level.setTile(level.getWidth() - 1, y, 0x222222, true); // Right wall
        }
        // Add a test solid block inside for collision testing
        level.setTile(10, 10, 0x990000, true);
        level.setTile(11, 10, 0x990000, true);
        level.setTile(10, 11, 0x990000, true);
        level.setTile(11, 11, 0x990000, true);
    }

    private int createPlayer() {
        BufferedImage sheet = AssetLoader.loadImage("/assets/sprites/player.png");
        BufferedImage[][] walkFrames = new BufferedImage[4][3]; // 4 directions, 3 frames each
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                walkFrames[col][row] = AssetLoader.getSprite(sheet, col, row, 32);
            }
        }

        // Example: down-facing, standing still (first row, first column)
        //BufferedImage playerSprite = AssetLoader.getSprite(sheet, 1, 0, 32);
        int playerEntity = entityManager.createEntity();
        entityManager.addComponent(playerEntity, new PositionComponent(100, 100));
        entityManager.addComponent(playerEntity, new VelocityComponent(0, 0));
        entityManager.addComponent(playerEntity, new SpriteComponent(walkFrames[0][0]));
        entityManager.addComponent(playerEntity, new AnimationComponent(walkFrames, 5));
        entityManager.addComponent(playerEntity, new PlayerComponent());
        entityManager.addComponent(playerEntity, new ColliderComponent(28, 28, -16, 0));

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

        // 2. Movement System: Applies the (potentially modified by collision) velocity to the position.
        movementSystem.update();

        // 3. Other Systems: Update based on the new state.
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