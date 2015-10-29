package com.burtonkrahn.pageapplet.scene;

import java.lang.*;

public class Animator {
    public static double now() {
	return (double)System.currentTimeMillis() / 1000;
    }

    protected boolean finished = false;
    protected boolean startTimeSet = false;
    protected double  startTime;
    protected double  lastStepTime;
    protected double  currentTime;

    public Animator() {
	reset();
    }

    public void destroy() {
    }

    public double timeSinceStart() {
        return this.currentTime - this.startTime;
    }

    public double timeSinceStep() {
        return this.currentTime - this.lastStepTime;
    }

    public void animate(double secs) {
	if( !this.startTimeSet ) {
	    this.startTime = secs;
	    this.startTimeSet = true;
	}
        this.lastStepTime = this.currentTime;
        this.currentTime = secs;
    }

    public void reset() {
	this.startTimeSet = false;
	this.finished = false;
    }

    public void setFinished() {
	this.finished = true;
	this.onFinished();
    }

    public boolean isFinished() {
	return this.finished;
    }

    /** override me to hook when finished */
    public void onFinished() {
    }

}

