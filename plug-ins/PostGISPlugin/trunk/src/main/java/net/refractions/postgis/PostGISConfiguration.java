/*
 * $Id: PostGISConfiguration.java,v 1.1.1.1 2004/01/06 00:13:15 pramsey Exp $
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
 */
package net.refractions.postgis;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.Extension;
/**
 * This class installs the PostGIS plugin. 
 */
public class PostGISConfiguration extends Extension {
  public void configure(PlugInContext context) {
    new PostGISPlugIn().initialize(context);    
  }
  
  public String getName() {
    return("PostGIS Driver (Refractions Research/HCU Hamburg, RZCN, V6.11.6B)");
  }
  
  public String getVersion() {
// UD, uwe.dalluege@rzcn.haw-hamburg.de
    return("");
  }
}