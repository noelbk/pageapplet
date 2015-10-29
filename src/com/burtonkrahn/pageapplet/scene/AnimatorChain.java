package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.util.*;

/** run an array of animators in serial, one after the other.  See
 * AnimatorArray to run a bunch in parallel */
public class AnimatorChain extends Animator {
    Animator[] animList;
    int animListIndex;
    
    public AnimatorChain(Animator[] list) {
        this.animList = list;
        this.animListIndex = 0;
    }
    
    public void animate(double t) {
        while( this.animListIndex < this.animList.length ) {
            Animator anim = this.animList[this.animListIndex];
            anim.animate(t);
            if( !anim.isFinished() ) {
                break;
            }
            this.animListIndex++;
            if( this.animListIndex >= this.animList.length ) {
                this.setFinished();
                break;
            }
        }
    }

    public void destroy() {
        for(int i=0; i<this.animList.length; i++) {
            this.animList[i].destroy();
        }
        this.animList = null;
        
        super.destroy();
    }

    public void reset() {
        super.reset();

        for(int i=0; i<this.animList.length; i++) {
            this.animList[i].reset();
        }
        this.animListIndex = 0;
    }
}
