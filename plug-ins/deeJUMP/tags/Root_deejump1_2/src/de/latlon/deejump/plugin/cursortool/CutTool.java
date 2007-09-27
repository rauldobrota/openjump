/*
 * From http://www.projet-sigle.org/
 * kindly provided by erwan.bocher@wanadoo.fr
 */

package de.latlon.deejump.plugin.cursortool;

import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.PolygonTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

public class CutTool extends PolygonTool
{
    final String wrongObjectSelectedMesg = "Wrong Object Selected"; 
    
    final String wrongObjectDrawnMesg = "Wrong Object Drawn"; 
    
    private FeatureDrawingUtil featureDrawingUtil;

    protected ImageIcon icon = null;
		
    protected CutTool(FeatureDrawingUtil featuredrawingutil)
    {
        featureDrawingUtil = featuredrawingutil;
    }

    public static CursorTool create(LayerNamePanelProxy layernamepanelproxy)
    {
        FeatureDrawingUtil featuredrawingutil = new FeatureDrawingUtil(layernamepanelproxy);
        return featuredrawingutil.prepare(new CutTool(featuredrawingutil), true);
    }

// Ici on va chercher l'icone pour le plugin. L'icone est localis� dans le repertoire de votre package

    public Icon getIcon()
    {
        return new ImageIcon(CutTool.class.getResource("cutpolygon.gif"));
    }

// Traitement r�alis� lors du mouvement de la sourie

    protected void gestureFinished()
        throws Exception
    {
        LayerViewPanel layerviewpanel = getWorkbench().getContext().getLayerViewPanel();
        
// Layer est la couche contenant l�objet � d�couper
        
        Collection editableLayers = layerviewpanel.getLayerManager().getEditableLayers(); 
        
        if( editableLayers.size() == 0 ){
            this.getWorkbench().getFrame().warnUser( "No *editable* layers found." );
            return;
        }
        
        Layer layer = (Layer)editableLayers.iterator().next();
        
        Feature feature = (Feature)layerviewpanel.getSelectionManager()
        	.getFeaturesWithSelectedItems(layer).iterator().next();
// On suppose que la couche contenant l�objet � d�couper est la seule couche �ditable

        // geom correspond � l'entit� g�om�trique d'entr�e, celle qui sera d�coup�e
        
        Geometry geom = feature.getGeometry();
        
        // getPolygon correspond au polygone d�ssin� et qui va servir � d�couper l'entit� d'entr�e
        Geometry geom1;
        try {
            geom1 = geom.intersection(getPolygon());
        } catch (IllegalArgumentException e) {
            layerviewpanel.getContext().warnUser( wrongObjectDrawnMesg );
            return;
        }
        // Ici on instancie geom2
        Geometry geom2 = geom1;
        
        // Ici on test si le polygone de d�coupe intersecte la g�om�trie d'entr�e
        // Si pas d'intersection on ne fait rien
        // Evite la cr�ation de g�om�trie vide
        
		if(!getPolygon().intersects(geom))
		 {

		 }
		 else {
	        
        // on verifie les conditions au niveau du type de geometry en entr�e
        // Il faut que ce soit un polygone ou une polyligne
        
        if((geom instanceof Polygon) || (geom instanceof MultiPolygon))
            {
                geom2 = geom.difference(getPolygon());
                
            }
        
        BasicFeature basicfeature = new BasicFeature(layer.getFeatureCollectionWrapper().getFeatureSchema());
        BasicFeature basicfeature1 = new BasicFeature(layer.getFeatureCollectionWrapper().getFeatureSchema());
        basicfeature.setGeometry(geom1);
        basicfeature1.setGeometry(geom2);
        
        // on suprime l'entit� d'entr�e que l'on remplace par les entit�s produites
        layer.getFeatureCollectionWrapper().remove(feature);
        layer.getFeatureCollectionWrapper().add(basicfeature);
        layer.getFeatureCollectionWrapper().add(basicfeature1);
        
//		rafra�chissement de l�affichage
        layerviewpanel.repaint();
    }
    }

    public void cancelGesture()
    {
    }
    public String getName() {
        return "Cut polygon";
    }

    public void mousePressed(final MouseEvent e) {
        if (!check(createEnableCheck())) {
            return;
        }
        super.mousePressed(e);
    }
    
    public void mouseReleased(final MouseEvent e) {
        if (!check( createEnableCheck())) {
            return;
        }    
        super.mouseReleased(e);
    
    }
    
    private EnableCheck createEnableCheck(){
        return new EnableCheck() {
            
            public String check(JComponent component) {
                SelectionManager sm = getPanel().getSelectionManager();
                
                Collection c = sm.getSelectedItems();
                
                if ( c.size() != 1 ){
                    return wrongObjectSelectedMesg;
                }
                
                try {
                    
                    Geometry o = (Geometry)c.iterator().next();
                    
                    if( !( (o instanceof Polygon) || (o instanceof MultiPolygon) ) ){
                        return wrongObjectSelectedMesg;    
                    }
                    
                } catch (Exception e) {
                    return e.toString(); 
                }
                
                return null;
            }

        };
    }
}