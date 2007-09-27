/**
 * 
 */
package de.latlon.deejump.plugin.cursortool;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.vividsolutions.jts.geom.Dimension;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

/**
 * @author ugo
 *
 */
public class MagnifyingGlassTool extends AbstractCursorTool {

    private JFrame frame;
    
    private final boolean inWindow = false;
    
    private final int SIZE = 50; 
    
    /**
     * 
     */
    public MagnifyingGlassTool() {
        super();
        
        if( inWindow ) {
            
            frame = new JFrame( "Mag" );
            frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
            frame.setSize( 2*SIZE, 2*SIZE );
            frame.setAlwaysOnTop( true );
            frame.setResizable( false );
            frame.addMouseMotionListener( new MouseMotionAdapter() {
            
                public void mouseMoved(MouseEvent e) {
                    MagnifyingGlassTool.this.mouseMoved(e);
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.cursortool.CursorTool#getCursor()
     */
    public Cursor getCursor() {
        return new Cursor( Cursor.CROSSHAIR_CURSOR );// createCursor(IconLoader.icon("MagnifyCursor.gif").getImage());
    }

    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.cursortool.CursorTool#getIcon()
     */
    public Icon getIcon() {
        return IconLoader.icon("Magnify.gif");
    }
    
    protected Shape getShape() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.cursortool.CursorTool#activate(com.vividsolutions.jump.workbench.ui.LayerViewPanel)
     */
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        if ( frame != null ) {
            frame.setVisible( true );
        }
    }

    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.cursortool.CursorTool#deactivate()
     */
    public void deactivate() {
        super.deactivate();
        if ( frame != null ) {
            frame.setVisible( false );
        }
    }

    protected void gestureFinished() throws Exception {
        
    }
    
    public void mouseClicked(MouseEvent e) {
        doImagePaint( e, false );
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
        doImagePaint( e, false );
    }

    public void mouseDragged(MouseEvent e) {
    	doImagePaint( e, true );
    }
    
    private void doImagePaint( MouseEvent e, boolean repaintPanel  ) {
        int h = getPanel().getHeight();
        int w = getPanel().getWidth();
        
        
        //must be done in activate
        BufferedImage copyImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = copyImage.createGraphics();
//        g2.copyArea( 25,30,100,100,100,100);
        
        getPanel().paint(g2);
        final int x = e.getX();
        final int y = e.getY();
        
        // half size is rubbish, make it full, and halve where necessary
        int halfWidth = SIZE/2;
        int halfHeight = SIZE/2;
        
        Point p = inWindow ? new Point(0,0) : e.getPoint();
        
        if ( !inWindow ) {
            p.translate( -2*halfWidth, -2*halfHeight );
        }
        
        int atX = x-halfWidth;
        int atY = y-halfHeight;
        
        w = 2*halfWidth;
        h = 2*halfHeight;

        //if out of bounds, ignore!!!!
        // should treat x and y separately...go around the edges to see what I mean...
        if ( atX < 0 || atY < 0 || (atX + w) > copyImage.getWidth() || (atY + h) > copyImage.getHeight() ) {
            return;
        }
        
        copyImage = copyImage.getSubimage( atX, atY, w, h );
        
        if ( repaintPanel ) {
            getPanel().repaint();
        }
        
//        Graphics g = getPanel().getGraphics();//frame.getContentPane().getGraphics();
        halfWidth *=4;
        halfHeight *=4;
        
        JComponent c = inWindow ? (JComponent)frame.getContentPane() : (JComponent)getPanel();
        paintZoomWindow( c, copyImage, p, halfWidth, halfWidth );
        
    }
    
    private void paintZoomWindow(  JComponent component, Image img, Point p, int width, int height) {
        
        Graphics g = component.getGraphics();
        
        g.setColor( Color.GRAY );
        g.drawImage(img, p.x, p.y, width, height, null);
        g.drawRect( p.x, p.y, width, height );
        g.dispose();
        
    }
    
}
