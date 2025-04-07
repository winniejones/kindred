package com.kindred.engine.entity.components;

import com.kindred.engine.entity.core.Component;

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