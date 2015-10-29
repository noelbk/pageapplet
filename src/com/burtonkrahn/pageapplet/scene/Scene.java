package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.util.*;
import java.awt.*; 
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.imageio.*;

import com.burtonkrahn.pageapplet.scene.*;

public class Scene 
    implements MouseListener, MouseMotionListener
{

    public static final String CONFIG_ANIMATE_SLEEP_KEY = "Scene.animateSleepSecs";
    public static final double CONFIG_ANIMATE_SLEEP_DEFAULT = 1.0/60;
    
    public static final String CONFIG_BACKGROUND_COLOR_KEY = "Scene.backgroundColor";
    public static final Color CONFIG_BACKGROUND_COLOR_DEFAULT = Color.white;

    public static final String CONFIG_FOREGROUND_COLOR_KEY = "Scene.foregroundColor";
    public static final Color CONFIG_FOREGROUND_COLOR_DEFAULT = Color.white;

    protected ConfigParams config;

    /** list of sprites to be drawn from bottom to top */
    protected LinkedList<Sprite> spriteList;

    protected Thread animatorThread;

    /** This gets the mouse event first, if set */ 
    protected MouseEventListener mouseEventCapture;
    protected MouseEventListener mouseHandledBy;

    protected boolean imageInvalidated;
    protected int imageType;
    protected BufferedImage image;
    protected Graphics2D imageGraphics;
    protected Dimension size;
    
    protected boolean destroyed = false;
    
    public Scene(Dimension size) {
        this(size,  BufferedImage.TYPE_INT_RGB);
    }

    public Scene(Dimension size, int imageType) {
        this.config = new ConfigParams();
        
        this.imageType = imageType;
        setSize(size);

	this.spriteList = new LinkedList<Sprite>();

	final Scene scene = this;
	this.animatorThread = new Thread( new Runnable() {
		public void run() {
		    try {
			while(!scene.isDestroyed()) {
			    scene.animateAndSleep();
			}
		    }
		    catch(InterruptedException e) {
		    }
		}
	    });
	this.animatorThread.setDaemon(true);
	this.animatorThread.start();

    }
    
    public void setConfig(ConfigParams config) {
        this.config = config;
    }

    public ConfigParams getConfig() {
        if( this.config == null ) {
            this.config = new ConfigParams();
        }
        return this.config;
    }

    public int getImageType() {
        return this.imageType;
    }

    public void setImageType(int value) {
        this.imageType = value;
        this.setSize(this.size);
    }
    
    public void setSize(Dimension size) {
        if( this.image != null ) {
            //this.image.dispose();
	    this.image = null;
        }
        if( this.imageGraphics != null ) {
            this.imageGraphics.dispose();
	    this.imageGraphics = null;
        }
        this.size = size;
        this.image = new BufferedImage(size.width, size.height, this.imageType);
        this.imageGraphics = (Graphics2D)this.image.getGraphics();
        this.imageGraphics.setRenderingHint
            (RenderingHints.KEY_ANTIALIASING, 
             RenderingHints.VALUE_ANTIALIAS_ON);
        this.imageGraphics.setRenderingHint
            (RenderingHints.KEY_RENDERING,    
             RenderingHints.VALUE_RENDER_QUALITY);
        invalidate();
    }

    public void destroy() {
	if( this.animatorThread != null ) {
	    this.animatorThread.interrupt();
	    try {
                this.animatorThread.join();
            }
            catch( InterruptedException e ) {
            }
	    this.animatorThread = null;
	}

	ListIterator li = spriteList.listIterator();
	while(li.hasNext()) {
	    ((Sprite)li.next()).destroy();
	}
	spriteList.clear();
	spriteList = null;
	
        if( this.image != null ) {
            //this.image.dispose();
	    this.image = null;
        }
        if( this.imageGraphics != null ) {
            this.imageGraphics.dispose();
	    this.imageGraphics = null;
        }
        this.destroyed = true;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    public void animateAndSleep() 
        throws InterruptedException
    {
	this.animate(Animator.now());
	Thread.sleep((long)(getAnimateSleepSeconds()*1000));
    }

    public double getAnimateSleepSeconds() {
	return this.config.getDouble(CONFIG_ANIMATE_SLEEP_KEY, 
                                     CONFIG_ANIMATE_SLEEP_DEFAULT);
    }

    public void animate(double secs) {
        ListIterator it = this.spriteList.listIterator();
        while(it.hasNext()) {
            ((Sprite)it.next()).animate(secs);
        }
    }

    /** give all future mouse requests to the sprite until mouseRelease */
    public void setMouseEventCapture(MouseEventListener listener) {
        this.mouseEventCapture = listener;
    }

    /** tell my container I need to be redrawn */
    public void invalidate() {
	this.imageInvalidated = true;
    }

    public Color getBackgroundColor() {
	return this.config.getColor(CONFIG_BACKGROUND_COLOR_KEY,
                                    CONFIG_BACKGROUND_COLOR_DEFAULT);
    }

    public Color getForegroundColor() {
	return this.config.getColor(CONFIG_FOREGROUND_COLOR_KEY,
                                    CONFIG_FOREGROUND_COLOR_DEFAULT);
    }


    /** get all my spriteList to draw to my back buffer from bottom to top */
    public void render() {
	if( !this.imageInvalidated ) {
	    return;
	}

        Graphics2D g = this.imageGraphics;
	Shape oldClip = g.getClip();
	AffineTransform oldTransform = g.getTransform();

	/** clear my back buffer */
        g.setColor(this.getBackgroundColor());
	g.fillRect(0, 0, this.size.width, this.size.height);
        g.setColor(this.getForegroundColor());

        DrawContext dc = new DrawContext(this.imageGraphics);
        ListIterator it = this.spriteList.listIterator();
        while(it.hasNext()) {
            ((Sprite)it.next()).transformAndDraw(dc);
        }

	g.setTransform(oldTransform);
	g.setClip(oldClip);
	
	this.imageInvalidated = false;
    }
    
    /** draw my back buffer to the screen */
    public void draw(Graphics2D g) {
	render();
        g.drawImage(this.image, null, 0, 0);
    }

    /** send mouse event to my capture first, then all other listeners
     * from top to bottom */
    public void onMouseEvent(MouseEvent e) {
        this.mouseHandledBy = null;
        
        if( this.mouseEventCapture != null ) {
            this.mouseEventCapture.onMouseEvent(e);
        }
        ListIterator it = this.spriteList.listIterator(this.spriteList.size() - 1);
        while(it.hasPrevious()) {
            ((MouseEventListener)it.previous()).onMouseEvent(e);
        }
    }

    public boolean mouseWasHandled() {
        return this.mouseHandledBy != null;
    }

    public void setMouseWasHandled(MouseEventListener l) {
        this.mouseHandledBy = l;
    }

    public void addSprite(Sprite sprite) {
        this.spriteList.add(sprite);
	this.invalidate();
    }

    public void removeSprite(Sprite sprite) {
        this.spriteList.remove(sprite);
	this.invalidate();
    }


    //--------------------------------------------------------------------
    // MouseListener

    public void mousePressed(MouseEvent e) {
        this.onMouseEvent(e);
    }

    public void mouseDragged(MouseEvent e) {
        this.onMouseEvent(e);
    }

    public void mouseReleased (MouseEvent e) {
        this.onMouseEvent(e);
    }

    public void mouseEntered (MouseEvent e) {
        this.onMouseEvent(e);
    }

    public void mouseExited (MouseEvent e) {
        this.onMouseEvent(e);
    }

    public void mouseClicked (MouseEvent e) {
        this.onMouseEvent(e);
    }

    public void mouseMoved(MouseEvent e) { 
        this.onMouseEvent(e);
    }

}

