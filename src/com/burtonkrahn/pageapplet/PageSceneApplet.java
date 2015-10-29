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

public class PageSceneApplet
    extends javax.swing.JApplet 
{ 
    private static final long serialVersionUID = 7526472295622776148L;

    protected JAppletScene scene;
    protected SpriteList pageList;
    protected PageSprite pageLeft;
    protected PageSprite pageRight;
    
    public void init() { 
        scene = new JAppletScene(this);

        pageList = new SpriteList();

        pageLeft = new PageSprite(scene);
        pageRight = new PageSprite(scene);

        pageLeft.setup(pageList, -1, PageSprite.PAGE_SIDE_LEFT, pageRight);
        pageRight.setup(pageList, 0, PageSprite.PAGE_SIDE_RIGHT, pageLeft);

        int i;
        for(i=0; i<4; i++) {
            String s;
            BufferedImage pageImage = null;
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

            System.out.println("loading image at: " + url.toString());
            try {
                pageImage = ImageIO.read(url);
            }
            catch( java.io.IOException e) {
                System.err.println("IOException: " + e);
            }
            
            if( pageImage != null ) {
                pageList.add(new ImageSprite(scene, pageImage) );
            }
        }

	Dimension d = getSize();
	int x = (d.width - 2*pageList.getSpriteSize().width) / 2;
	int y = (d.height - pageList.getSpriteSize().height) / 2;
	pageLeft.setLocalTransform(AffineTransform.getTranslateInstance(x, y));
	x += pageList.getSpriteSize().width;
	pageRight.setLocalTransform(AffineTransform.getTranslateInstance(x, y));
    } 

    public void paint(Graphics g)  {
        scene.draw((Graphics2D)g);
    }
} 

