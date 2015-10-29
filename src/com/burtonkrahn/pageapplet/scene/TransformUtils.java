package com.burtonkrahn.pageapplet.scene;

import java.awt.geom.*; 

public class TransformUtils {
    
    /** convert a 4x4 matrix to a 3x3 AffineTransform matrix */
    public static AffineTransform convertMatrix(Matrix4x4d m) {
	double[] v = m.vec;
	return new AffineTransform(
				   v[0], v[1],
				   v[4], v[5], 
				   v[12], v[13]
				   );

    }

    /** 
        transform screen point p to the local XY axis after my transform.

        use the dot product to project P on to my transformed X, Y
        axes, offset by my transformed origin O.

          Qx = ( P - O ) dot ( X - 0 )
          Qy = ( P - O ) dot ( Y - 0 )

        If my current transformation matrix M = 
          [ M0 M2 M4 ]
          [ M1 M3 M5 ]
          [  0  0  1 ]

        then Qx = (P-O) dot (X - O) boils down to:
        M0 * (Px - M4) + M1 * (Px - M5)
    */
    public static Vectord pointToLocalAxes(AffineTransform t, double px, double py) {
        double [] m = new double[6];
        t.getMatrix(m);
       
        Vectord q = new Vectord(2);
        q.vec[0] = px - m[4];
        q.vec[1] = py - m[5];
        px = m[0] * q.vec[0] + m[1] * q.vec[1];
        py = m[2] * q.vec[0] + m[3] * q.vec[1];
        q.vec[0] = px;
        q.vec[1] = py;

        return q;
    }
    
}
