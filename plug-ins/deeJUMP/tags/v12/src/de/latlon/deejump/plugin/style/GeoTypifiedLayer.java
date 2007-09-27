/*----------------    FILE HEADER  ------------------------------------------

This file is part of deegree.
Copyright (C) 2001 by:
EXSE, Department of Geography, University of Bonn
http://www.giub.uni-bonn.de/exse/
lat/lon Fitzke/Fretter/Poth GbR
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
lat/lon Fitzke/Fretter/Poth GbR
Meckenheimer Allee 176
53115 Bonn
Germany
E-Mail: poth@lat-lon.de

Jens Fitzke
Department of Geography
University of Bonn
Meckenheimer Allee 166
53115 Bonn
Germany
E-Mail: jens.fitzke@uni-bonn.de

                 
 ---------------------------------------------------------------------------*/
package de.latlon.deejump.plugin.style;

import java.util.Collections;
import java.util.List;

import com.vividsolutions.jump.workbench.model.Layer;

/**
 * ...
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class GeoTypifiedLayer extends Layer {

    private String geoType = "";
    private String wmsLayerName = "";
    private String styleName = ""; 
    private String styleTitle = "";
    private String featureTypeStyle = ""; 

    public GeoTypifiedLayer( Layer layer, String wmsLayerName, String styleName, String styleTitle, String featureTypeStyle, String geoType){
        super( layer.getName(), 
            layer.getBasicStyle().getFillColor(),
            layer.getFeatureCollectionWrapper().getWrappee(),
            layer.getLayerManager() );
        
        setWmsLayerName( wmsLayerName );
        setStyleName( styleName );
        setStyleTitle( styleTitle );
        setFeatureTypeStyle( featureTypeStyle );
        setGeoType( geoType );
    }
    
    public String getFeatureTypeStyle() {
        return featureTypeStyle;
    }
    public String getStyleName() {
        return styleName;
    }
    public String getGeoType() {
        return geoType;
    }
    public void setGeoType( String geoType ) {
        this.geoType = geoType;
    }
    public void setFeatureTypeStyle( String featureTypeStyle ) {
        this.featureTypeStyle = featureTypeStyle;
    }
    public void setStyleName( String styleName ) {
        this.styleName = styleName;
    }
    public void setStyleTitle( String layerTitle ) {
        this.styleTitle = layerTitle;
    }
    public String getWmsLayerName() {
        return wmsLayerName;
    }
    public void setWmsLayerName( String wmsLayerName ) {
        this.wmsLayerName = wmsLayerName;
    }
    public String getStyleTitle() {
        return styleTitle;
    }
    
	public List getStyles() {
		return super.getStyles();
	}

    
}
