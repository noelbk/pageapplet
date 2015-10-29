package com.burtonkrahn.pageapplet;

import java.awt.*;
import java.awt.event.*;

import com.burtonkrahn.pageapplet.scene.*;

/** draw a single page, and spawn a PageFoldSprite when dragged */
class PageSprite extends Sprite {

    //--------------------------------------------------------------------
    // constants

    public static final String CONFIG_FOLD_TIME_KEY = "PageSprite.foldTime";
    public static final double CONFIG_FOLD_TIME_DEFAULT = .200;

    public static final String CONFIG_CURL_HIT_WIDTH_KEY = 
	"PageSprite.curlHitWidth";
    public static final double CONFIG_CURL_HIT_WIDTH_DEFAULT = .5;

    public static final int PAGE_SIDE_RIGHT = 1;
    public static final int PAGE_SIDE_LEFT = 2;

    //--------------------------------------------------------------------
    // member variables

    protected SpriteList pageList;
    protected Dimension pageSize;
    protected int pageIndex;
    protected int pageIncrement;
    protected Vectord[] fixedPoints;
    protected int pageSide; /* PAGE_SIDE_{ RIGHT | LEFT } */
    protected Vectord curlHitRect;
    protected PageFoldSprite curlFoldSprite;
    protected boolean curling = false;

    protected Vectord foldPoint0; 
    protected Vectord foldPoint1;
    protected Vectord foldPointDest;
    protected PageSprite otherPage; /* my other half */

    //--------------------------------------------------------------------
    // construction
    public PageSprite(Scene scene) {
        super(scene);
    }
    
    public void setup(SpriteList pageList
                      ,int pageIndex
                      ,int pageSide 
                      ,PageSprite otherPage
                      ) {
        this.pageList  = pageList;
        this.pageIndex = pageIndex;
        this.pageSide  = pageSide;
        this.otherPage = otherPage;
	this.pageSize  = pageList.getSpriteSize();

        int curlHitWidth = (int)(this.getCurlHitWidth() * this.pageSize.width);

        if( pageSide == PAGE_SIDE_LEFT ) {
            this.pageIncrement = 1;
            this.fixedPoints = new Vectord[] {
                new Vectord(new double[] {0, 0})
                ,new Vectord(new double[] {0, this.pageSize.height})
            };
            this.foldPointDest = new Vectord(new double[] {
                -this.pageSize.width, this.pageSize.width/2, 0
            });
            this.curlHitRect = new Vectord(new double[] {
                this.pageSize.width-curlHitWidth, 0, 
                this.pageSize.width+curlHitWidth, this.pageSize.width
            });
        }
        else if ( pageSide == PAGE_SIDE_RIGHT ) {
            this.pageIncrement = -1;
            this.fixedPoints = new Vectord[] {
                new Vectord(new double[] {this.pageSize.width, 0})
                ,new Vectord(new double[] {this.pageSize.width, this.pageSize.width})
            };
            this.foldPointDest = new Vectord(new double[] {
                2*this.pageSize.width, this.pageSize.width/2, 0
            });
            this.curlHitRect = new Vectord(new double[] {
                -curlHitWidth, 0, 
                curlHitWidth, this.pageSize.width
            });
        }
    }

    //--------------------------------------------------------------------
    // get / set properties

    public double getFoldSpeed() {
        return this.pageSize.width / 
	    this.getConfig().getDouble(CONFIG_FOLD_TIME_KEY, 
				       CONFIG_FOLD_TIME_DEFAULT);
    }
    

    public double getCurlHitWidth() {
        return this.pageSize.width * 
	    this.getConfig().getDouble(CONFIG_CURL_HIT_WIDTH_KEY, 
				       CONFIG_CURL_HIT_WIDTH_DEFAULT);
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public void setPageIndex(int index) {
        this.pageIndex = index;
        this.invalidate();
    }

    public Vectord getFoldPoint0() {
	return this.foldPoint0;
    }

    public Vectord getFoldPoint1() {
	return this.foldPoint1;
    }

    public Vectord getFoldPointDest() {
	return this.foldPointDest;
    }

    public PageSprite getOtherPage() {
	return this.otherPage;
    }

    //--------------------------------------------------------------------
    // overrides

    /** draw my current page */
    public void draw(DrawContext dc) {
        Sprite page = this.pageList.get(this.pageIndex);
        if( page != null ) {
            page.draw(dc);
        }
    }

    protected void curlUp() {
        if( this.curlFoldSprite == null ) {
            this.curlFoldSprite = new PageFoldSprite(this);
            this.pageIndex = this.pageIndex + 2 * this.pageIncrement;
        }
        else {
            this.foldPoint0 = this.curlFoldSprite.getFoldPoint0();
            this.foldPoint1 = this.curlFoldSprite.getFoldPoint1();
        }

        /* curl up the folded page and leave it curled */
        PageSprite pageSprite = this;
        this.curlFoldSprite.setAnimator
            (new VectorSpeedAnimator(foldPoint0, foldPoint1, getFoldSpeed()) {
                    public void animate(double secs) {
                        super.animate(secs);
                        curlFoldSprite.setFoldPoint1(getCurrent());
                    }
                }
             );
    }

    protected void curlDown() {
        /* curl down the folded page, delete it, and fix my page index */

        final PageSprite pageSprite = this;
        final PageFoldSprite curlFoldSprite = this.curlFoldSprite;

        curlFoldSprite.setAnimator
            (new VectorSpeedAnimator(curlFoldSprite.foldPoint1, 
                                     curlFoldSprite.foldPoint0,
                                     getFoldSpeed()) {
                    public void animate(double secs) {
                        super.animate(secs);
			curlFoldSprite.setFoldPoint1(getCurrent());
                    }
                    public void onFinished() {
                        curlFoldSprite.foldTo(pageSprite);
                        pageSprite.curlFoldSprite = null;
                        curlFoldSprite.destroy();
                    }
                }
             );
    }

    public boolean curlHitTest(Vectord p) {
	double w = this.getCurlHitWidth();

	boolean hit;
	
	
	hit = p.vec[1] >= -w && p.vec[1] < this.pageSize.height + w;

	if( hit ) {
	    if( this.pageSide == PAGE_SIDE_LEFT ) {
		hit = p.vec[0] >= -w &&  p.vec[0] <= w;
	    }
	    else {
		hit = p.vec[0] >= this.pageSize.width - w &&  
		    p.vec[0] <= this.pageSize.width + w ;
	    }
	}
        return hit;
    }

    public void onMouseEvent(MouseEvent e) {
        if( this.getScene().mouseWasHandled() ) {
            return;
        }
        
        Vectord mousePoint = this.toLocalAxes(e);

        if( e.getID() == MouseEvent.MOUSE_MOVED ) {
            if( curlHitTest(mousePoint) ) {
		this.getScene().setMouseWasHandled(this);
                this.scene.setMouseEventCapture(this);

                /* hover over edge -> start curl */
                Vectord edgePoint0 = new Vectord(0, 0, 0);
                Vectord edgePoint1 = new Vectord(0, this.pageSize.width, 0);
                if( this.pageSide == PAGE_SIDE_LEFT ) {
                    edgePoint0.vec[0] += this.pageSize.width;
                    edgePoint1.vec[0] += this.pageSize.width;
                }
                Vectord edgeLine = edgePoint1.copy().sub(edgePoint0);
                
                /* project the mouse position onto my curling edge */
                Vectord v;
                double l;
                v = mousePoint.copy();
                v.sub(edgePoint0);
                l = v.dot(edgeLine) / edgeLine.length();
                if( l < 0 ) l = 0;
                if( l > 1 ) l = 1;
                edgeLine.scale(l);
                foldPoint0 = edgePoint0.copy();
                foldPoint0.add(edgeLine);
                
                /* curl towards foldPointDest */
                Vectord foldVec;
                foldVec = foldPoint0.copy().sub(foldPointDest).normalize();
                foldVec.scale( 2 * getCurlHitWidth() );
                foldPoint1 = foldPoint0.copy().add(foldVec);
                
                if( this.curlFoldSprite == null ) {
                    this.curlFoldSprite = new PageFoldSprite(this);
                }
                this.curlFoldSprite.curlUp(foldPoint0, foldPoint1);
		this.curling = true;

            }
            else if( this.curling ) {
                /* move away from curl -> stop curl */
		this.getScene().setMouseWasHandled(this);
                this.scene.setMouseEventCapture(null);
		this.curling = false;
		
		if( this.curlFoldSprite != null ) {
		    this.curlFoldSprite.curlDown();
		}
            }
        }
        else if( e.getID() == MouseEvent.MOUSE_PRESSED ) {
            if( this.curlFoldSprite != null ) {
		this.getScene().setMouseWasHandled(this);

                /* let my pageFoldSprite take over the mouse drag */
                this.curlFoldSprite.takeMouseDrag(e);
                this.curlFoldSprite = null;
            }
        }
    }
    
    public void curlFoldSpriteDone(PageFoldSprite curlFoldSprite) {
	if( curlFoldSprite == this.curlFoldSprite ) {
	    this.curlFoldSprite = null;
	    this.curling = false;
	}
    }

}
