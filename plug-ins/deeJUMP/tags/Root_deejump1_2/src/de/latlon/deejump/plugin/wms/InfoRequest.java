/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package de.latlon.deejump.plugin.wms;

import java.awt.geom.Point2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.wms.BoundingBox;
import com.vividsolutions.wms.MapRequest;

import de.latlon.deejump.plugin.wfs.WFSResearchPlugIn;

/**
 * Represents all of the parameters of a GetFeatureInfo request from a WMS server.
 * This code has been adapted from MapRequest.java
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a> 
 */
public class InfoRequest {
    
    private static Logger LOG = Logger.getLogger( InfoRequest.class );

	private int xPos;
	private int yPos;
	private List layerList;
	private String format;
	private String serverURL;
	private BoundingBox bbox;  
	private int imgWidth;
	private int imgHeight;

  /**
   * Creates a new InfoRequest.
   * @param serverURL the server address
   */
  public InfoRequest(String serverURL) {
    this.serverURL = serverURL;  
    format = null;
  }

  /**
   * Returns the image format of this request.
   *
   * @return the string representing the image format of this request
   */
  public String getFormat() {
    return format;
  }

  /**
   * Returns the x-coordinate of pixel where the user clicked on the image
   * @return the x-position of the click
   * */
  public int getRequestX() {
    return xPos;
  }

  /**
   * Sets x value of the click.
   * @param x the x-position of the click
   */
  public void setRequestX(int x) {
    this.xPos = x;
  }

  /**
   * Returns the list of layers to be requested. Each item in the
   * list should be a String which is the name of a layer.
   * @return the list of layer names to be requested
   */
  public List getLayers() {
      //<<TODO:NAMING>> Might be clearer to name this method getLayerNames [Jon Aquino]
      return Collections.unmodifiableList(layerList);
  }

  /**
   * Sets y value of the click.
   * @param y the y-position of the click
   */
  public void setRequestY( int y ) {
    this.yPos = y;
  }

  /**
   * Returns the y-coordinate of pixel where the user clicked on the image
   * @return the y position of the click
   * */
  public int getRequestY( ) {
    return yPos;
  }

  
  /**
   * Sets the format of this request. The format must be a string which is in
   * the list of supported formats as provided by getSupportedFormatList()
   * (not necessarily the same String object, but the same sequence of characters).
   * This will be 
   * a mime-type string for a WMS 1.1 server (image/gif, image/jpeg, image/png).
   *
   * @param format a format string which is in the list of supported formats
   *
   */
  public void setFormat( String format ) throws IllegalArgumentException {
      this.format = format;   
  }


  /**
   * Sets the width and height of the image being requested.
   * @param x the x-position of the click    
   * @param y the y-position of the click   
   * */
  public void setClickXY( int x, int y) {
    this.xPos = x;
    this.yPos = y;
  }

  /**
   * Sets size of the map image being requested information about.
   * @param w the image width    
   * @param h the image height
   * */
  public void setImgDim( int w, int h) {
      this.imgHeight = h;
      this.imgWidth = w;
    }

  /**
   * Sets the layers to be requested. Each item in the list should be a string
   * which corresponds to the name of a layer. The order of the list is
   * important as the layers are rendered in the same order they are listed.
   * @param layerList an ordered List of the names of layers to be displayed
   */
  public void setLayers( List layerList ) {
    //<<TODO:NAMING>> Might be clearer to name this method setLayerNames [Jon Aquino]
    this.layerList = layerList;
  }

  
  /**
   * Returns the full URL of the GetFeatureInfo request
   * @param a URL containing server adress and servlet request
   */
  public URL getURL() throws MalformedURLException {

    StringBuffer urlBuf = new StringBuffer();  // UT changes to ver. 1.1.1.
    urlBuf.append( serverURL + "REQUEST=GetFeatureInfo&VERSION=1.1.1&X=" + xPos + "&Y=" + yPos + "&WIDTH=" + imgWidth + "&HEIGHT=" + imgHeight);
    urlBuf.append( "&QUERY_LAYERS=" ).append( MapRequest.listToString( layerList ) );
    urlBuf.append( "&LAYERS=" ).append( MapRequest.listToString( layerList ) );
    //if( format != null ) {
      urlBuf.append( "&FORMAT=" + "image/jpeg" );
    //}
    urlBuf.append( "&INFO_FORMAT=application/vnd.ogc.gml" );
    if( bbox != null ) {
        urlBuf.append( "&BBOX=" + bbox.getMinX() + "," + bbox.getMinY()
                      + "," + bbox.getMaxX() + "," + bbox.getMaxY() );
        if( bbox.getSRS() != null && !bbox.getSRS().equals( "LatLon" ) ) {
          urlBuf.append( "&SRS=" + bbox.getSRS() );
        }
    }
    LOG.info( "GetFeatureInfo: " + urlBuf.toString());
    return new URL( urlBuf.toString() );
  }

  
  /**
   * Sets the bounding box of the map.
   * @param bbox the bounding box
   * */
  public void setBoundingBox( BoundingBox bbox ) {
      this.bbox = bbox;
    }
  
  /**
   * Gets the BoundingBox of the image being requested.
   * @return the BoundingBox of the image being requested
   */
  public BoundingBox getBoundingBox() {
    return bbox;
  }
  
  public static InfoRequest createFeatureInfoRequest(WMSLayer wmsLayer, int imgWidth, int imgHeight, Point2D clickXY,
          Point2D lowLeftP, Point2D upRightP){
      
      InfoRequest ir = new InfoRequest( wmsLayer.getServerURL() );
      ir.setLayers( wmsLayer.getLayerNames() );
      ir.setClickXY((int)clickXY.getX(), (int)clickXY.getY());
		ir.setImgDim(imgWidth,imgHeight );

		BoundingBox b = new BoundingBox(
		        wmsLayer.getSRS(),
		        (float)lowLeftP.getX(), (float)lowLeftP.getY(),
		        (float)upRightP.getX(), (float)upRightP.getY());
		ir.setBoundingBox(b);

		return ir;
  }

}
