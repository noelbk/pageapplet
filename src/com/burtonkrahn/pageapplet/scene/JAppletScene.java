package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.util.*;
import java.awt.*; 
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;

public class JAppletScene
    extends Scene 
{
    protected JApplet applet;

    public JAppletScene(JApplet applet) {
        super(applet.getSize(),  BufferedImage.TYPE_INT_RGB);

	this.applet = applet;
        this.applet.addMouseListener(this);
        this.applet.addMouseMotionListener(this);
    }

    /** tell my container I need to be redrawn */
    public void invalidate() {
	super.invalidate();
	this.applet.invalidate();
    }
}

