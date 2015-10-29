package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.awt.*; 
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;

import com.burtonkrahn.pageapplet.scene.*;

public class ImageSprite 
    extends Sprite
{
    protected BufferedImage image;

    public ImageSprite(Scene scene, BufferedImage image) {
        super(scene);
        
        this.image = image;
        this.setSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    public void draw(DrawContext dc) {
        dc.getGraphics().drawImage(this.image, null, 0, 0);
    }
}

