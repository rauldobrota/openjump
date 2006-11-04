/*
 * Created on 29.06.2005 for PIROL
 *
 * SVN header information:
 *  $Author$
 *  $Rev: 2434 $
 *  $Date$
 *  $Id$
 */
package de.fhOsnabrueck.jump.pirol.plugIns.EditAttributeByFormula;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * Class to load the EditAttributeByFormulaPlugIn.
 *
 * @author Ole Rahn
 * <br>
 * <br>FH Osnabr&uuml;ck - University of Applied Sciences Osnabr&uuml;ck,
 * <br>Project: PIROL (2005),
 * <br>Subproject: Daten- und Wissensmanagement
 * 
 * @version $Rev: 2434 $
 * 
 */
public class EditAttributeByFormulaExtension extends Extension {

    /**
     *@inheritDoc
     */
    public void configure(PlugInContext context) throws Exception {
        new EditAttributeByFormulaPlugIn().initialize(context);
    }

}
