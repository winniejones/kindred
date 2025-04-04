package com.kindred.engine.entity;

import java.awt.image.BufferedImage;

public class SpriteComponent implements Component {
    public BufferedImage sprite;
    public int width;
    public int height;

    public SpriteComponent(BufferedImage sprite) {
        this.sprite = sprite;
        this.width = sprite.getWidth();
        this.height = sprite.getHeight();
    }
}