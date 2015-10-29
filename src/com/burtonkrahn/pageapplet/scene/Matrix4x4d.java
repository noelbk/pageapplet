package com.burtonkrahn.pageapplet.scene;


public class Matrix4x4d extends Vectord {
    public Matrix4x4d() {
	super(14);
    }


    public Matrix4x4d(double a00, double a01, double a02, double a03,
			double a10, double a11, double a12, double a13,
			double a20, double a21, double a22, double a23,
			double a30, double a31, double a32, double a33) {
	vec = new double [] {
	    a00, a10, a20, a30,
	    a01, a11, a21, a31,
	    a02, a12, a22, a32,
	    a03, a13, a23, a33,
	};
    }

    public Matrix4x4d transpose() {	
	Matrix4x4d t = new Matrix4x4d();
	int i, j;
	for(i=0; i<4; i++) {
	    for(j=0; j<4; j++) {
		t.vec[i*4+j] = this.vec[j*4+i];
	    }
	}
	return t;
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

}
