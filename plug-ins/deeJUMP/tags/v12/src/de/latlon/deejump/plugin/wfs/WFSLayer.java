/*----------------    FILE HEADER  ------------------------------------------

Copyright (C) 2001-2005 by:
lat/lon GmbH
http://www.lat-lon.de

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Contact:

Andreas Poth
lat/lon GmbH
Aennchenstraße 19
53177 Bonn
Germany


 ---------------------------------------------------------------------------*/

package de.latlon.deejump.plugin.wfs;

import java.awt.Color;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;


/**
 * This class represents a WFS layer. The important difference to a common
 * Layer is that a WFS layer keeps hold of its original WFS name no matter
 * how JUMP changes it. This original name (FeatureType name)is used for 
 * transactions.<br/>
 * Another addition concerns a layer listener. By setting the WFSLayerListener
 * one can keep track of the changes performed to the layer and use those
 * changes for creation transaction statements.
 * <br/>
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class WFSLayer extends Layer {
    
    private String origName;
    
    /**
     * The layer listener that keeps track of changes.
     */
    private WFSLayerListener layerListener; 
    
    // FIXME remove is now in datasource
    private String serverURL; 
    
    private String geoPropertyName;
    
    /**
     * This contructor should not be used.*/
    public WFSLayer(){
        super();
    }

    /**
     * Creates a new WFS layer from its display name, a color, some data, and
     * using a layer manager ad an original (WFS) layer name. This constructor
     * is similar to one of <code>Layer</code>, but include the original layer 
     * name as parameter.
     * @param displayName name to be displayed and used by JUMP
     * @param fillColor start-up fill color
     * @param featureCollection the data
     * @param layerManager the layer manager responsible for this layer
     * @param origName the WFS layer name (the name of the FeatureType)
     */
    public WFSLayer(String displayName, Color fillColor,
            FeatureCollection featureCollection, LayerManager layerManager,
            String origName, String geoPropertyName){
        super(displayName, fillColor,featureCollection, layerManager);
        this.origName = origName;
        this.geoPropertyName = geoPropertyName;
    }
    
    public String getOriginalName(){ return this.origName; }
    
    /**
     * Gets the layer listener
     * @return the layer listener associated with this WFSLayer
     */
    public WFSLayerListener getLayerListener() {
        return layerListener;
    }
    
    /**
     * Sets the layer listener
     * @param layerListener the layer listener
     */
    public void setLayerListener(WFSLayerListener layerListener) {
        this.layerListener = layerListener;
    }
    
    
    public String getServerURL() {
        return serverURL;
    }
    
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }
    public String getGeoPropertyName() {
        return geoPropertyName;
    }
    
    public boolean hasReadableDataSource() {
         return true;
    }

    public String getOrigName() {
        return origName;
    }

    public void setOrigName(String origName) {
        this.origName = origName;
    }

    public void setGeoPropertyName(String geoPropertyName) {
        this.geoPropertyName = geoPropertyName;
    }
    
    
}
