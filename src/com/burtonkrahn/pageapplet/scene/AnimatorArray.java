package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.util.*;

/** run an array of animators in parallel, all together.  See
 * AnimatorChain to run a bunch in serial */
public class AnimatorArray extends Animator {
    Animator[] animList;
    
    public AnimatorArray(Animator[] list) {
        this.animList = list;
    }
    
    public Animator[] getList() {
        return this.animList;
    }

    public void animate(double t) {
        boolean finished = true;
        for(int i=0; i<this.animList.length; i++) {
            Animator anim = this.animList[i];
            if( !anim.isFinished() ) {
                this.animList[i].animate(t);
                if( !anim.isFinished() ) {
                    finished = false;
                }
            }
        }
        if( finished ) {
            setFinished();
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
    }
}
