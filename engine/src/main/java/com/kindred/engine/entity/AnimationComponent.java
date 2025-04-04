package com.kindred.engine.entity;

import java.awt.image.BufferedImage;

public class AnimationComponent implements Component {
    public BufferedImage[][] frames; // frames[direction][frameIndex]
    public int direction = 0; // 0: down, 1: left, 2: right, 3: up
    public int frame = 0;
    public int tick = 0;
    public int frameDelay = 10;

    public AnimationComponent(BufferedImage[][] frames, int frameDelay) {
        this.frames = frames;
        this.frameDelay = frameDelay;
    }

    public BufferedImage getCurrentFrame() {
        return frames[direction][frame];
    }

    public void update() {
        tick++;
        if (tick >= frameDelay) {
            tick = 0;
            frame = (frame + 1) % frames[direction].length;
        }
    }

    public void setDirection(int newDirection) {
        if (newDirection != direction) {
            direction = newDirection;
            frame = 0;
            tick = 0;
        }
    }
}