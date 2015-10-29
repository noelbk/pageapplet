package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.awt.*; 
import java.awt.geom.*;

import com.burtonkrahn.pageapplet.scene.*;


public class DrawContext {
    protected Graphics2D graphics;

    public DrawContext(Graphics2D g) {
        this.graphics = g;
    }

    public Graphics2D getGraphics() {
        return this.graphics;
    }

}

