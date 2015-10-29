package com.burtonkrahn.pageapplet;

import com.burtonkrahn.pageapplet.scene.*;

/** move at constant speed from v0 to v1, and stop at v1 */
public class VectorSpeedAnimator extends Animator {
    protected Vectord v0, v1;
    protected Vectord speedvec;
    protected Vectord current;
    protected double maxlength;

    public VectorSpeedAnimator(Vectord v0, Vectord v1, double unitsPerSec) {
        this.v0 = v0.copy();
        this.v1 = v1.copy();
        this.speedvec = v1.copy().sub(v0);
        this.maxlength = this.speedvec.length();
        this.speedvec.normalize().scale(unitsPerSec);
        this.current = v0.copy();
    }

    public void animate(double secs) {
        super.animate(secs);
        if( this.isFinished() ) {
            return;
        }
	current.add(this.speedvec.copy().scale(this.timeSinceStart()));
        if( current.length() > this.maxlength ) {
            this.current = this.v1;
            this.setFinished();
        }
    }

    public void reset() {
        super.reset();
        this.current = this.v0;
    }

    protected Vectord getCurrent() {
        return this.current;
    }

}
