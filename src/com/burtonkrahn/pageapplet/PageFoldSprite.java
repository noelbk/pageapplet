package com.burtonkrahn.pageapplet;

import java.lang.*;
import java.util.*;
import java.awt.event.*;

import com.burtonkrahn.pageapplet.scene.*;

public class PageFoldSprite extends Sprite {
    //--------------------------------------------------------------------
    // constants

    //--------------------------------------------------------------------
    // member variables

    protected SpriteList    pageList;
    protected int         pageIndex;
    protected int         pageIncrement;
    protected Vectord[] fixedPoints;
    protected PageSprite  pageFrom;
    protected PageSprite  pageTo;
    protected Vectord   foldPoint0;
    protected Vectord   foldPoint1;
    protected Vectord   foldPointDest;

    //--------------------------------------------------------------------
    // construction

    public PageFoldSprite(PageSprite pageFrom) {
        /* I'm a child of pageFrom */
	super(pageFrom.getScene());
        setTransformParent(pageFrom);

        this.pageFrom = pageFrom;
        this.pageTo = pageFrom.getOtherPage();
        this.foldPoint0 = pageFrom.getFoldPoint0();
        this.foldPoint1 = pageFrom.getFoldPoint1();
        this.foldPointDest = pageFrom.getFoldPointDest();

        this.pageList = pageFrom.pageList;
        this.pageIndex = pageFrom.pageIndex;
        this.pageIncrement = pageFrom.pageIncrement;
        this.fixedPoints = pageFrom.fixedPoints;
    }

    //--------------------------------------------------------------------
    // get/set

    public double getFoldSpeed() {
	return this.pageFrom.getFoldSpeed();
    }

    public Vectord getFoldPoint0() {
	return this.foldPoint0;
    }

    public void setFoldPoint0(Vectord v) {
	this.foldPoint0 = v;
        this.invalidate();
    }

    public Vectord getFoldPoint1() {
	return this.foldPoint1;
    }

    public void setFoldPoint1(Vectord v) {
	this.foldPoint1 = v;
        this.invalidate();
    }

    //--------------------------------------------------------------------
    // overrides

    /** draw pageIndex and pageIndex + pageIncrement folded between
     * foldPoint0 and foldPoint1 */
    public void draw(DrawContext dc) {
    }

    /** set pageDest's pageIndex and destroy myself */
    public void foldTo(PageSprite pageDest) {
        pageDest.setPageIndex(this.pageIndex);
        this.destroy();
    }

    /** drag foldPoint1 around. Animate to foldPointDest on release */
    public void onMouseEvent(MouseEvent e) {
        if( this.getScene().mouseWasHandled() ) {
            return;
        }

	Vectord mousePoint = this.toLocalAxes(e);
        
        if( e.getID() == MouseEvent.MOUSE_DRAGGED ) {
	    this.getScene().setMouseWasHandled(this);

            this.foldPoint1 = mousePoint;
            this.invalidate();
        }
        else if( e.getID() == MouseEvent.MOUSE_RELEASED ) {
	    this.getScene().setMouseWasHandled(this);

            /* fold over if the dot product of the fold and fold-over
             * direction is not negative */
            boolean doFold = 
                foldPoint1.copy().sub(foldPoint0)
                .dot(foldPointDest.copy().sub(foldPoint0)) 
                > 0;
            
            if( doFold ) {
                /* animate myself until folded over completely, then
                 * set pageTo's index */
                final PageFoldSprite pageFoldSprite = this;
                setAnimator
                    (new VectorSpeedAnimator(foldPoint1, foldPointDest, getFoldSpeed()) {
                            public void animate(double secs) {
                                super.animate(secs);
                                pageFoldSprite.setFoldPoint1(getCurrent());
                            }
                            public void onTerminate() {
                                pageFoldSprite.foldTo(pageTo);
                            }
                        }
                     );
            }
            else {
                /* aborted the fold, go back to pageFrom */
                this.foldTo(pageFrom);
            }
        }
    }


    /** start curling */
    public void curlUp(Vectord foldPoint0, Vectord foldPoint1) {
        final PageFoldSprite pageFoldSprite = this;

        setAnimator(animateFoldPoints(foldPoint0, foldPoint1));
    }

    /** return an animator that animates both my fold points */
    protected Animator animateFoldPoints(Vectord foldDest0, Vectord foldDest1) {
        final PageFoldSprite pageFoldSprite = this;
        return new AnimatorArray(new Animator[] {
            new VectorSpeedAnimator(pageFoldSprite.getFoldPoint0(),
                                    foldDest0,
                                    pageFoldSprite.getFoldSpeed()
                                    ) {
                public void animate(double secs) {
                    super.animate(secs);
                    pageFoldSprite.setFoldPoint0(this.getCurrent());
                }
            },
            new VectorSpeedAnimator(pageFoldSprite.getFoldPoint1(),
                                    foldDest1,
                                    pageFoldSprite.getFoldSpeed()
                                    ) {
                public void animate(double secs) {
                    super.animate(secs);
                    pageFoldSprite.setFoldPoint1(this.getCurrent());
                }
            }
        });
    }


    /** curl down, then set my from page's index and destroy myself */
    public void curlDown() {
        final PageFoldSprite pageFoldSprite = this;
        setAnimator(new AnimatorChain(new Animator[] {
            animateFoldPoints(foldPoint0, foldPoint0),
            new Animator() {
                public void animate(double secs) {
                    /* done curling down, set my from page's index and destroy myself */
                    super.animate(secs);
                    pageFoldSprite.foldTo(pageFoldSprite.pageFrom);
		    pageFoldSprite.pageFrom.curlFoldSpriteDone(pageFoldSprite);
                }
            }
        }));
    }

    /** when the user drags on my parent page, it hands the mouse to me */
    public void takeMouseDrag(MouseEvent e) {
        this.scene.setMouseEventCapture(this);
        this.setAnimator(null);
        this.onMouseEvent(e);
    }

}
    
