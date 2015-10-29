package com.burtonkrahn.pageapplet.scene;

import java.lang.Math.*;

public class Vectord {
    public double [] vec;

    public Vectord() {
    }

    public Vectord(int nelts) {
	vec = new double[nelts];
	for(int i=0; i<nelts; i++) {
	    vec[i] = 0;
	}
    }

    public Vectord(double [] v) {
	vec = new double[v.length];
        System.arraycopy(v, 0, vec, 0, v.length);
    }

    public Vectord(double [] v, int start, int len) {
	vec = new double[len];
        System.arraycopy(v, start, vec, 0, len);
    }
    
    public Vectord(Vectord v) {
	vec = (double[])v.vec.clone();
    }

    public Vectord(double p0, double p1, int nelts) {
	vec = new double[nelts];
	int i;
	for(i=0; i<nelts; i++) {
	    vec[i] = p0 + ((double)i)/(nelts-1) * (p1-p0);
	}
    }

    public static Vectord fromRange(double p0, double p1, int nelts) {
	double[] v = new double[nelts];
	int i;
	for(i=0; i<nelts; i++) {
	    v[i] = p0 + ((double)i)/(nelts-1) * (p1-p0);
	}
        return new Vectord(v);
    }

    public static Vectord from3(double p0, double p1, double p2) {
        return new Vectord(new double[] {p0, p1, p2});
    }

    public static Vectord from4(double p0, double p1, double p2, double p3) {
        return new Vectord(new double[] {p0, p1, p2, p3});
    }


    public static Vectord concat(Vectord a, Vectord b) {
	Vectord c = new Vectord(a.nelts() + b.nelts());
        System.arraycopy(a.vec, 0, c.vec, 0, a.nelts());
        System.arraycopy(b.vec, 0, c.vec, a.nelts(), b.nelts());
	return c;
    }

    public int nelts() {
	return vec.length;
    }

    public double dot(Vectord v) {
	int i, n;
	double d;
	d = 0;
	for(i=0; i < vec.length && i < v.vec.length; i++) {
	    d += vec[i] * v.vec[i];
	}
	return d;
    }

    public double length() {
	return Math.sqrt(dot(this));
    }

    public Vectord normalize() {
        this.scale(1.0/this.length());
	return this;
    }

    public Vectord copy() {
        return new Vectord(this.vec);
    }

    public Vectord scale(double s) {
	int i;
	for(i=0; i < vec.length; i++) {
	    vec[i] *= s;
	}
        return this;
    }

    public Vectord add(Vectord v) {
	int i;
	for(i=0; i < vec.length && i < v.vec.length; i++) {
	    vec[i] += v.vec[i];
	}
        return this;
    }

    public Vectord sub(Vectord v) {
	int i;
	for(i=0; i < vec.length && i < v.vec.length; i++) {
	    vec[i] -= v.vec[i];
	}
        return this;
    }
    
    public Vectord mul(Vectord v) {
	int i;
	for(i=0; i < vec.length && i < v.vec.length; i++) {
	    vec[i] *= v.vec[i];
	}
        return this;
    }

    public Vectord div(Vectord v) {
	int i;
	for(i=0; i < vec.length && i < v.vec.length; i++) {
	    vec[i] /= v.vec[i];
	}
        return this;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        for( int i = 0; i < vec.length; i++ ) {
            if( i > 0 ) {
                sb.append(", ");
            }
            sb.append(vec[i]);
        }
        sb.append("]");
        return sb.toString();
    }

};

