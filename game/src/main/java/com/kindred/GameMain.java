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

    private EntityManager entityManager;
    private MovementSystem movementSystem;
    private AnimationSystem animationSystem;

    private RenderSystem renderSystem;
    private CameraSystem cameraSystem;

    private int playerEntity;
    private int cameraEntity;

    private Level level;

    public GameMain() {
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true);
        requestFocus();

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        screen = new Screen(WIDTH, HEIGHT);
        keyboard = new Keyboard();

        addKeyListener(keyboard);

        entityManager = new EntityManager();
        movementSystem = new MovementSystem(entityManager);
        animationSystem = new AnimationSystem(entityManager);
        renderSystem = new RenderSystem(entityManager, screen);
        cameraSystem = new CameraSystem(entityManager, screen);

        // Create level
        level = new Level(50, 30, 32);
        level.generateTestMap();
        addLevelBoundaries();

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
        int entity = entityManager.createEntity();
        entityManager.addComponent(playerEntity, new PositionComponent(100, 100));
        entityManager.addComponent(playerEntity, new VelocityComponent(0, 0));
        entityManager.addComponent(playerEntity, new SpriteComponent(walkFrames[0][0]));
        entityManager.addComponent(playerEntity, new AnimationComponent(walkFrames, 5));
        entityManager.addComponent(playerEntity, new PlayerComponent());

        return entity;
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
        VelocityComponent vel = entityManager.getComponent(playerEntity, VelocityComponent.class);
        PositionComponent pos = entityManager.getComponent(playerEntity, PositionComponent.class);

        if (pos == null || vel == null) {
            System.err.println("Player entity missing Position or Velocity component!");
            return; // Can't proceed
        }

        // Calculate desired velocity based on input for THIS FRAME
        int desiredVx = 0;
        int desiredVy = 0;

        if (keyboard.up) desiredVy = -2;
        if (keyboard.down) desiredVy = 2;
        if (keyboard.left) desiredVx = -2;
        if (keyboard.right) desiredVx = 2;

        // --- Collision Detection ---
        int hitboxSize = 28;
        int tileSize = level.getTileSize();

        // Store final velocity to apply after collision checks
        int finalVx = desiredVx;
        int finalVy = desiredVy;

        // Check horizontal collision based on DESIRED horizontal movement
        if (desiredVx != 0) { // Only check if trying to move horizontally
            if (isColliding(pos.x, pos.y, desiredVx, 0, hitboxSize, tileSize)) {
                // System.out.println("Collision X Detected!"); // Debug
                finalVx = 0; // Collision detected, stop horizontal movement
                // Optional: Add sliding logic here by adjusting pos.x slightly
                // e.g., pos.x = (tileX + 1) * tileSize; or pos.x = tileX * tileSize - hitboxSize;
            }
        }

        // Check vertical collision based on DESIRED vertical movement
        // IMPORTANT: Use the ORIGINAL position (pos.x, pos.y) for this check,
        // not a potentially adjusted one if sliding were implemented above.
        if (desiredVy != 0) { // Only check if trying to move vertically
            if (isColliding(pos.x, pos.y, 0, desiredVy, hitboxSize, tileSize)) {
                // System.out.println("Collision Y Detected!"); // Debug
                finalVy = 0; // Collision detected, stop vertical movement
                // Optional: Add sliding logic here by adjusting pos.y slightly
            }
        }

        // --- Update Velocity Component ---
        // Set the final calculated velocity (potentially zeroed by collision)
        // into the component. MovementSystem will use this.
        vel.vx = finalVx;
        vel.vy = finalVy;

        // --- Update Systems ---
        // MovementSystem reads the final vel.vx/vy and updates pos.x/y
        movementSystem.update();
        // Other systems update based on the new state
        cameraSystem.update(); // Camera might follow the updated player position
        animationSystem.update(); // Animation might change based on final velocity/state
    }

    /**
     * Checks for collision between a hitbox at a potential future position and solid tiles.
     * Assumes (x, y) is the top-left corner of the hitbox.
     *
     * @param x          Current X position (top-left of hitbox).
     * @param y          Current Y position (top-left of hitbox).
     * @param xa         Potential horizontal movement offset for this check.
     * @param ya         Potential vertical movement offset for this check.
     * @param hitboxSize The width and height of the collision hitbox.
     * @param tileSize   The size (width/height) of a single map tile.
     * @return True if a collision is detected, false otherwise.
     */
    private boolean isColliding(int x, int y, int xa, int ya, int hitboxSize, int tileSize) {
        if (tileSize <= 0) {
            System.err.println("Tile size is zero or negative, cannot perform collision check.");
            return true; // Prevent movement if tile size is invalid
        }
        // Check all 4 corners of the hitbox's potential future position
        for (int c = 0; c < 4; c++) {
            // Calculate corner offsets relative to the hitbox top-left
            int cornerXOffset = (c % 2) * (hitboxSize - 1);
            int cornerYOffset = (c / 2) * (hitboxSize - 1);

            // Calculate the absolute world coordinates of the corner *after* the potential move
            int futureCornerX = x + xa + cornerXOffset;
            int futureCornerY = y + ya + cornerYOffset;

            // Convert the world coordinates of the corner to tile coordinates
            int tileX = futureCornerX / tileSize;
            int tileY = futureCornerY / tileSize;

            // Check if the tile at these coordinates is solid
            if (level.isSolid(tileX, tileY)) {
                // System.out.println("Collision: Corner " + c + " at world (" + futureCornerX + "," + futureCornerY + ") -> tile (" + tileX + "," + tileY + ")"); // Detailed debug
                return true; // Collision detected
            }
        }
        // No collision detected at any of the four corners
        return false;
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy(); // Get the buffer strategy for drawing
        if (bs == null) {
            createBufferStrategy(3); // Create triple buffering if it doesn't exist
            return; // Exit render call for this frame
        }

        // Prepare screen for drawing
        screen.clear(); // Clear the pixel buffer (e.g., fill with black or background color)

        // Get camera position/offset (assuming CameraSystem updates Screen's offset)
        // cameraSystem.update() should have set the screen's render offset
        // Or get CameraComponent: CameraComponent cam = entityManager.getComponent(cameraEntity, CameraComponent.class);
        // screen.setOffset(cam.x, cam.y);

        // Render the level tiles (relative to camera)
        // Assuming level.render takes the screen to draw onto
        level.render(screen); // This needs to account for camera offset

        // Render entities (relative to camera)
        renderSystem.render(); // RenderSystem should handle camera offset

        // --- Debug Rendering ---
        PositionComponent playerPos = entityManager.getComponent(playerEntity, PositionComponent.class);
        if (playerPos != null) {
            // Draw player hitbox outline (yellow) - adjust for camera if needed
            screen.drawRect(playerPos.x, playerPos.y, 28, 28, 0xFFFF00, true); // Hitbox size = 28
            // Draw player position marker (small red cross) - adjust for camera
            screen.fillRect(playerPos.x - 1, playerPos.y - 1, 3, 3, 0xFF0000, true);
        }
        // Debug: highlight solid tiles in red overlay (relative to camera)
        // This needs camera offset from screen or CameraComponent
        int camX = screen.xOffset; // Example of getting offset
        int camY = screen.yOffset;
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                if (level.isSolid(x, y)) {
                     screen.drawRect(x * level.getTileSize() - camX, y * level.getTileSize() - camY, level.getTileSize(), level.getTileSize(), 0x99FF0000, false); // Semi-transparent red outline
                }
            }
        }
        // --- End Debug Rendering ---


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