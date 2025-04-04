package com.kindred;

import com.kindred.engine.entity.EntityManager;
import com.kindred.engine.entity.MovementSystem;
import com.kindred.engine.entity.PositionComponent;
import com.kindred.engine.entity.VelocityComponent;
import com.kindred.engine.entity.SpriteComponent;
import com.kindred.engine.input.Keyboard;
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
    private int playerEntity;

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

        // Create player entity with position and velocity
        playerEntity = createPlayer();
    }

    private int createPlayer() {
        BufferedImage sheet = AssetLoader.loadImage("/assets/sprites/player.png");

        // Example: down-facing, standing still (first row, first column)
        BufferedImage playerSprite = AssetLoader.getSprite(sheet, 1, 0, 32);
        int entity = entityManager.createEntity();
        entityManager.addComponent(playerEntity, new PositionComponent(100, 100));
        entityManager.addComponent(playerEntity, new VelocityComponent(0, 0));
        entityManager.addComponent(playerEntity, new SpriteComponent(playerSprite));

        return entity;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    public synchronized void stop() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

            while (delta >= 1) {
                update();
                updates++;
                delta--;
            }
            render();
            frames++;

            if (System.currentTimeMillis() - timer >= 1000) {
                frame.setTitle(TITLE + " | UPS: " + updates + ", FPS: " + frames);
                updates = 0;
                frames = 0;
                timer += 1000;
            }
        }
        stop();
    }

    private void update() {
        keyboard.update();
        VelocityComponent vel = entityManager.getComponent(playerEntity, VelocityComponent.class);

        vel.vx = 0;
        vel.vy = 0;
        if (keyboard.up) vel.vy = -4;
        if (keyboard.down) vel.vy = 4;
        if (keyboard.left) vel.vx = -4;
        if (keyboard.right) vel.vx = 4;

        movementSystem.update();
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        screen.clear();

        PositionComponent pos = entityManager.getComponent(playerEntity, PositionComponent.class);
        //screen.fillRect(pos.x, pos.y, 64, 64, 0xff00ff00, false); // green rectangle
        SpriteComponent sprite = entityManager.getComponent(playerEntity, SpriteComponent.class);
        screen.drawSprite(pos.x, pos.y, sprite.sprite);

        System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);

        Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
        g.dispose();
        bs.show();
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