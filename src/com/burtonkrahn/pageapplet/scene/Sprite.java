package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.awt.*; 
import java.awt.geom.*;
import java.awt.event.*;

public class Sprite 
    implements MouseEventListener 
{
    protected Scene scene;
    protected String name;
    protected AffineTransform localTransform; /** my local transform */
    protected Sprite transformParent; /** transform could be relative to a parent */
    protected Animator animator;
    protected Dimension size;

    public Sprite(Scene scene) {
        this.localTransform = new AffineTransform();
	this.animator = null;
	this.size = size;
        this.setScene(scene);
    }

    public void destroy() {
	if( this.animator != null ) {
	    this.animator.destroy();
	}
	this.scene.removeSprite(this);
    }

    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Dimension getSize() {
	return this.size;
    }

    public void setSize(Dimension size) {
	this.size = size;
    }

    public void transformAndDraw(DrawContext dc) {
        Graphics2D g = dc.getGraphics();
        
        // apply my local transform
        AffineTransform oldTransform = g.getTransform();
        g.transform(getTransform());

        /** draw myself or test for a hit */
        draw(dc);
        
        g.setTransform(oldTransform);
    }
    
    public void draw(DrawContext dc) {
        // override me
    }

    public void invalidate() {
        this.scene.invalidate();
    }

    public Scene getScene() {
	return this.scene;
    }

    public void setScene(Scene scene) {
	if( this.scene != null ) {
	    this.scene.removeSprite(this);
	}
	this.scene = scene;
	if( this.scene != null ) {
	    this.scene.addSprite(this);
	}
    }

    public ConfigParams getConfig() {
        return this.getScene().getConfig();
    }

    public Sprite getTransformParent() {
        return this.transformParent;
    }

    public void setTransformParent(Sprite parent) {
        this.transformParent = parent;
    }

    /** get parents trabsform plus my local transform */
    public AffineTransform getTransform() {
	AffineTransform t;
	if( this.transformParent != null ) {
	    t = this.transformParent.getTransform();
	}
	else {
	    t = new AffineTransform();
	}
	t.concatenate(this.localTransform);
        return t;
    }

    public AffineTransform getLocalTransform() {
	return this.localTransform;
    }

    public void setLocalTransform(AffineTransform t) {
        this.localTransform = t;
	this.invalidate();
    }

    /** add a transform to myself */
    public void addLocalTransform(AffineTransform t) {
        this.localTransform.concatenate(t);
	this.invalidate();
    }

    public Animator getAnimator() {
	return this.animator;
    }

    public void setAnimator(Animator a) {
	this.animator = a;
    }

    public void animate(double secs) {
        if( this.animator != null ) {
            this.animator.animate(secs);
            if( this.animator.isFinished() ) {
                this.animator.destroy();
                this.animator = null;
            }
        }
    }

    public void onMouseEvent(MouseEvent e) {
    }

    public Vectord toLocalAxes(MouseEvent e) {
        return TransformUtils.pointToLocalAxes(getTransform(), e.getX(), e.getY());
    }

}

