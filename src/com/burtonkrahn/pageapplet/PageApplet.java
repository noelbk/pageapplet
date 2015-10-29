// http://java.sun.com/developer/technicalArticles/Media/imagestrategies/
// http://java.sun.com/j2se/1.4.2/docs/api/java/awt/Graphics2D.html

package com.burtonkrahn.pageapplet;

import java.awt.*; 
import java.awt.image.*;
import java.lang.*;
import java.math.*;
import java.net.*;
import javax.imageio.*;
import java.awt.geom.*;
import java.awt.event.*;

import com.burtonkrahn.pageapplet.scene.*;

public class PageApplet 
    extends javax.swing.JApplet 
    implements MouseListener, MouseMotionListener
{ 
    private static final long serialVersionUID = 7526472295622776147L;

    protected Vectord foldP0 = null;
    protected Vectord foldP1 = null;
    protected BufferedImage pageImage[];
    protected int pageIndex;
    protected double pageWidth;
    protected double pageHeight;

    protected BufferedImage offImage;
    protected Graphics2D offGraphics;
    protected Dimension offDimension;
    
    protected int marginX = 20;
    protected int marginY = 100;

    public void init() { 
        addMouseListener(this);
        addMouseMotionListener(this);

        pageImage = new BufferedImage[4];
        pageImage[0] = null;
        pageImage[1] = null;
        pageImage[2] = null;
        
        int i;
        for(i=0; i<4; i++) {
            String s;
            URL url = null;

            try {
                s = getParameter("pageUrl" + i);
                if( s == null ) {
                    url = new URL(getCodeBase(), "page00" + i + ".jpg");
                }
                else {
                    url = new URL(getCodeBase(), s);
                }
            }
            catch( java.net.MalformedURLException e) {
                System.err.println("MalformedURLException: " + e);
            }

	    // debug
            System.out.println("loading image at: " + url.toString());

            try {
                pageImage[i] = ImageIO.read(url);
            }
            catch( java.io.IOException e) {
                System.err.println("IOException: " + e);
            }
            pageWidth = pageImage[0].getWidth();
            pageHeight = pageImage[0].getHeight();
            pageIndex = 1;
        }
        repaint();
    } 

    public Vectord getMouseVector(float x, float y) {
        return Vectord.from3((double)x - pageWidth - marginX, 
                               (double)y - marginY, (double)0);
    }

    public void mousePressed(MouseEvent e) {
        foldP0 = getMouseVector(e.getX(), e.getY());
        foldP1 = null;
        repaint();
    }

    public void mouseDragged(MouseEvent e) 
    {
        if( foldP0 != null ) {
            foldP1 = getMouseVector(e.getX(), e.getY());
        }
        repaint();
    }

    public void mouseReleased (MouseEvent e) {
        foldP0 = null;
        foldP1 = null;
        repaint();
    }

    public void mouseEntered (MouseEvent e) {
    }

    public void mouseExited (MouseEvent e) {
    }

    public void mouseClicked (MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) 
    { 
    }

    // Performs the double-buffering operations; updates one frame
    public void paint(Graphics g)
    {
        Dimension d = getSize();
		
        // Create the offscreen graphics context
        if ((offGraphics == null) || (d.width != offDimension.width) || (d.height != offDimension.height)) {
            offDimension = d;
            offImage = (BufferedImage)createImage(d.width, d.height);
            offGraphics = (Graphics2D)offImage.getGraphics();
            offGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            offGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
        }

        // Erase the previous image
        offGraphics.setPaint(getBackground());
        offGraphics.fillRect(0, 0, d.width, d.height);

        // Paint the frame into the image
        drawAll(offGraphics);

        // Paint the image onto the screen
        g.drawImage(offImage, 0, 0, null);		
    }

    public void drawAll(Graphics2D g) { 
        AffineTransform oldTransform = g.getTransform();

        g.transform(AffineTransform.getTranslateInstance(marginX, marginY));

        Paint oldPaint = g.getPaint();
        g.setPaint(Color.black);
        int coverMargin = 5;
        g.fillRect(-coverMargin, -coverMargin, (int)(2*(pageWidth+coverMargin)), (int)(pageHeight + 2*coverMargin));
        g.setPaint(oldPaint);

        g.drawImage(pageImage[0], null, 0, 0);

        int spineShadowWidth = 3;
        Paint spineShadowPaint = new GradientPaint((int)pageWidth, 0, new Color(0, 0, 0, 255), 
                                                   (int)pageWidth-spineShadowWidth, 0, new Color(0, 0, 0, 0));
        g.setPaint(spineShadowPaint);
        g.fillRect((int)pageWidth-spineShadowWidth, 0, (int)spineShadowWidth, (int)pageHeight);

        g.transform(AffineTransform.getTranslateInstance(pageWidth, 0));

        AffineTransform preFoldTransform = g.getTransform();
        Shape preFoldClip = g.getClip();

        drawFold(g);

        // funky trick: reflect teh whole folded page
        if( false ) {
            g.setClip(preFoldClip);
            g.setTransform(preFoldTransform);
            
            Planed reflectPlane = new Planed(Vectord.from3((double)0, (double)pageHeight,     (double)0),
                                                 Vectord.from3((double)0, (double)pageHeight + 1, (double)0));
            Matrix4x4d m = reflectPlane.getReflectMatrix();
            AffineTransform reflectTransform = 
                TransformUtils.convertMatrix(m);
            g.transform(reflectTransform);
            
            drawFold(g);
        }
        
        g.setTransform(oldTransform);
    } 


    protected void drawPage(Graphics2D g, BufferedImage img) {
        if( img != null ) {
            g.drawImage(img, null, 0, 0);
        }
    }

    // constrain the fold so it doesn't rip off any fixed points.
    // Returns the minimum distance to a fixed point
    protected double constrainFold(Planed fold_plane, Vectord [] fixed_points) {
        double min_dist = 0;
        for(int i=0; i<fixed_points.length; i++) {
            double d = fold_plane.distanceFromPoint(fixed_points[i]);
            if( i == 0 || d < min_dist ) {
                min_dist = d;
            }
        }
        if( min_dist < 0 ) {
            fold_plane.translate(-min_dist);
        }
        return min_dist;
    }
 
    // constrain the fold so it doesn't rip off any fixed points.
    // Returns the minimum distance to a fixed point
    protected double[] foldMinMaxDist(Planed foldPlane, 
                                      Vectord [] fixedPoints) {
        double minDist = 0;
        double maxDist = 0;
        for(int i=0; i<fixedPoints.length; i++) {
            double d = foldPlane.distanceFromPoint(fixedPoints[i]);
            if( i == 0 || d < minDist ) {
                minDist = d;
            }
            if( i == 0 || d > maxDist ) {
                maxDist = d;
            }
        }
        return new double[] { minDist, maxDist };
    }

    protected void drawFold(Graphics2D g) {
        Planed foldPlane = null;
	Matrix4x4d m;
        // maximum length of a fold
        double foldLengthMax = pageWidth * pageWidth 
            + pageHeight * pageHeight;  
        double[] foldDist = null;
        
        if( foldP0 != null && foldP1 != null ) {
	    // the fold plane bisects p0 and p1;
	    Vectord p0 = new Vectord(foldP0);
	    p0.add(foldP1);
	    p0.scale(.5);
	    foldPlane = new Planed(p0, foldP1); 
            
            // constrain the foldPlane so it doesn't rip the pages
            // off.
	    Vectord[] fixedPoints = {
                new Vectord(new double[] { 0, 0, 0 } )
                ,new Vectord(new double[] { 0, pageHeight, 0 } )
            };
            
            foldDist = foldMinMaxDist(foldPlane, fixedPoints);
            if( foldDist[0] < 0 ) {
                foldPlane.translate(-foldDist[0]);
                foldDist = foldMinMaxDist(foldPlane, fixedPoints);
            }
        }
        
        if( foldPlane == null ) {
	    // just draw the top page
	    drawPage(g, pageImage[pageIndex+0]);

            int spineShadowWidth = 6;
            Paint spineShadowPaint = new GradientPaint((int)0, 0, new Color(0, 0, 0, 255), 
                                                       (int)spineShadowWidth, 0, new Color(0, 0, 0, 0));
            g.setPaint(spineShadowPaint);
            g.fillRect((int)0, 0, (int)spineShadowWidth, (int)pageHeight);
	}
	else {
            m = foldPlane.getAxisMatrix();
            AffineTransform foldAxisTransform = 
                TransformUtils.convertMatrix(m);

	    // bottom-most page
	    drawPage(g, pageImage[pageIndex+2]);
            
            // current page (underneath fold) 
	    if( true ) {
		Shape oldClip = g.getClip();
		AffineTransform oldTransform = g.getTransform();
                
		// clip along fold
		g.transform(foldAxisTransform);
		g.clipRect((int)-foldLengthMax/2, 0, 
                           (int)foldLengthMax, (int)foldLengthMax);
		g.setTransform(oldTransform);

		// draw main page
		drawPage(g, pageImage[pageIndex+0]);
                
		g.setClip(oldClip);
		g.setTransform(oldTransform);
	    }
	    
	    // gradient shadows on both sides of fold
	    if( true ) {
		Shape oldClip = g.getClip();
		AffineTransform oldTransform = g.getTransform();
                
		// clip to page
		g.clipRect(0, 0, (int)pageWidth, (int)pageHeight);
                
                // draw shadows under fold
		g.transform(foldAxisTransform);

                // gradient shadow under fold
                float foldShadowUnderWidth = 6; 
                Paint foldShadowUnderPaint = 
                    new GradientPaint(0, 0, 
                                      new Color(0, 0, 0, 255), 
                                      0, foldShadowUnderWidth, 
                                      new Color(0, 0, 0, 0));
                g.setPaint(foldShadowUnderPaint);
                
                g.fillRect((int)-foldLengthMax/2, 0, 
                           (int)foldLengthMax, (int)foldShadowUnderWidth);
                g.transform(AffineTransform.getScaleInstance(1, -1));
                g.fillRect((int)-foldLengthMax/2, 0, 
                           (int)foldLengthMax, (int)foldShadowUnderWidth);
                
                g.setTransform(oldTransform);
                g.setClip(oldClip);
            }

            // shadow in crease along spine
            if( true ) {
                int spineShadowWidth = 6;
                Paint spineShadowPaint = 
                    new GradientPaint((int)0, 0, 
                                      new Color(0, 0, 0, 255), 
                                      (int)spineShadowWidth, 0, 
                                      new Color(0, 0, 0, 0));
                g.setPaint(spineShadowPaint);
                g.fillRect((int)0, 0, (int)spineShadowWidth, (int)pageHeight);
            }

	    // folded-over page
	    if( true ) {
		Shape oldClip = g.getClip();
		AffineTransform oldTransform = g.getTransform();
                Paint oldPaint = g.getPaint();

		// clip along fold
		g.transform(foldAxisTransform);
		AffineTransform clippedTransform = g.getTransform();
		g.clipRect((int)-foldLengthMax/2, 0, 
                           (int)foldLengthMax, (int)foldLengthMax);
		g.setTransform(oldTransform);

                // projected shadow under folded page
                if( true ) {
                    Paint foldShadowPaint = new Color(0, 0, 0, 64);

                    // project shadow by overprojecting across fold
                    double x = (foldDist[1] - pageWidth) / pageWidth;
                    x = x * x - 1;
                    x = 1 - x * x;

                    // debug
                    //System.out.println("foldDist[1]=" + foldDist[1] + " x=" + x);
                    

                    m = foldPlane.getTranslateMatrix(-2 - x);
                    AffineTransform foldShadowTransform = TransformUtils.convertMatrix(m);
                    g.transform(foldShadowTransform);
                    
                    g.setPaint(foldShadowPaint);
                    g.fillRect(0, 0, (int)pageWidth, (int)pageHeight);
                    g.setTransform(oldTransform);
                }

		// page reflected across fold plane
                m = foldPlane.getReflectMatrix();
                AffineTransform foldReflectTransform = TransformUtils.convertMatrix(m);
                g.transform(foldReflectTransform);

                // have to reflect the page after folding, or it will
                // be reversed
                g.transform(AffineTransform.getScaleInstance(-1, 1));
                g.transform(AffineTransform.getTranslateInstance(-pageWidth, 0));
                AffineTransform reflectedTransform = g.getTransform();
		g.clipRect(0, 0, (int)pageWidth, (int)pageHeight);

		drawPage(g, pageImage[pageIndex+1]);
		
                // gradient highlight along fold
                float foldHighlightWidth = 12;
                Paint foldHighlightPaint = 
                    new GradientPaint(0, 0, 
                                      new Color(255, 255, 255, 255), 
                                      0, foldHighlightWidth, 
                                      new Color(255, 255, 255, 0));
		g.setTransform(clippedTransform);
                g.setPaint(foldHighlightPaint);
		g.fillRect((int)-foldLengthMax/2, 0, 
                           (int)foldLengthMax, (int)foldHighlightWidth);

		g.setClip(oldClip);

                g.setPaint(oldPaint);
		g.setTransform(oldTransform);
	    }
	}
    }
} 

