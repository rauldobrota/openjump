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
package it.ama_mi.sis.framework.db.openjump;

//import java.util.Locale;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.vividsolutions.jump.workbench.JUMPWorkbench;

/**
 * Singleton for the Internationalization (I18N)
 * <pre>
 * [1] HOWTO TRANSLATE JUMP IN MY OWN LANGUAGE
 *  Copy theses files and add the locales extension for your language and country instead of the *.
 *  - resources/jump_*.properties
 *  - com/vividsolutions/jump/workbench/ui/plugin/KeyboardPlugIn_*.html
 *
 * [2] HOWTO TRANSLATE MY PLUGIN AND GIVE THE ABILITY TO TRANSLATE IT
 *  Use theses methods to use your own *.properties files :
 *  @see it.ama_mi.sis.framework.db.openjump.SISI18N#setPlugInRessource(String, String)
 *  @see it.ama_mi.sis.framework.db.openjump.SISI18N#get(String, String)
 *  @see it.ama_mi.sis.framework.db.openjump.SISI18N#getMessage(String, String, Object[])
 *  
 *  And use jump standard menus
 *  @see com.vividsolutions.jump.workbench.ui.MenuNames
 *
 * Code example :
 * <code>
 * public class PrintPlugIn extends AbstractPlugIn
 *  {
 *    private String name = "print";
 *
 * public void initialize(PlugInContext context) throws Exception
 * {
 *   I18N.setPlugInRessource(name, "org.agil.core.jump.plugin.print");
 *   context.getFeatureInstaller().addMainMenuItem(this,
 *                                                 new String[]
 *                                                 {MenuNames.TOOLS, 
 *   											   I18N.get(name, "print")}
 *                                                 , I18N.get(name, "print"), false, null, null);
 * }
 * ...
 * </code>
 * </pre>
 * 
 * TODO :I18N (1) Improve translations
 * TODO :I18N (2) Separate config (customization) and I18N
 * TODO :I18N (3) Explore and discuss about I18N integration and Jakarta Common Ressources
 * (using it as a ressource interface)
 * 
 * @author Basile Chandesris - <chandesris@pt-consulting.lu>
 * 
 * @see com.vividsolutions.jump.workbench.ui.MenuNames 
 * @see com.vividsolutions.jump.workbench.ui.VTextIcon text rotation)
 */
public final class SISI18N {
	private static Logger LOG = Logger.getLogger(SISI18N.class);
	private static final class SingletonHolder {
		static final SISI18N _singleton = new SISI18N();
	  }

	/*
	 * * use 'jump<locale>.properties' i18n mapping file */
	// STanner changed the place where are stored bundles. Now are in /language
	// public static ResourceBundle rb = ResourceBundle.getBundle("com.vividsolutions.jump.jump");
	//public static ResourceBundle rb = ResourceBundle.getBundle("language/jump");
	public static ResourceBundle rb;
	static {
		try {
			rb = ResourceBundle.getBundle("language/jump");
		} catch (Exception e) {
				//if couldn't find specific language, use default
			try {
				rb = ResourceBundle.getBundle("com.vividsolutions.jump.jump");
			} catch (Exception e2) {
				rb = null;					//no bundle found at all
			}
		}
	}
	
	public static Hashtable plugInsResourceBundle = new Hashtable();

	  private SISI18N() {}

	  public static SISI18N getInstance() 
	  {
		return SingletonHolder._singleton;
	  }
	  
	  /**
	   * Load file specified in command line (-i18n lang_country)
	   * (lang_country :language 2 letters + "_" + country 2 letters)
	   * Tries first to extract lang and country, and if only lang
	   * is specified, loads the corresponding resource bundle.
	   * @param langcountry
	   */
	  public static void loadFile(String langcountry)
	  {
	  	String lang = langcountry.split("_")[0];
		LOG.debug(lang);
		
		try {
			String country = langcountry.split("_")[1];
			LOG.debug(country);
			Locale locale = new Locale(lang, country);		 	  	
			rb = ResourceBundle.getBundle("language/jump", locale);
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			LOG.debug(e.getMessage());
			Locale locale = new Locale(lang);		 	  	
			rb = ResourceBundle.getBundle("language/jump", locale);
		}
	  }
	  
	  public String get(Class clazz,String label) {
	  	try {
				return rb != null ? 
						get(clazz.getName() + "." + label) : label;
			} catch (MissingResourceException e) {
				return label;
			}
	  }
	  
	  /**
	   * Process text with the locale 'jump_<locale>.properties' file
	   * @param label
	   * @return i18n label
	   * If no resourcebundle is found, returns default string contained
	   * inside com.vividsolutions.jump.jump
	   */
	  public static String get(String label)
	  {
	     try {
	          return rb.getString(label);
	     } catch (java.util.MissingResourceException e) {
	         String default_translation=ResourceBundle.getBundle("com.vividsolutions.jump.jump").getString(label);
	         LOG.debug(e.getMessage()+" default_value:"+ default_translation);
	         return default_translation; 
	      }
	  }

	  /**
	   * Get the short signature for locale 
	   * (letters extension :language 2 letters + "_" + country 2 letters)
	   * @return string signature for locale
	   */
	  public static String getLocale()
	  {
	    return rb.getLocale().getLanguage()+"_"+rb.getLocale().getCountry();
	  }
		  
	  /**
	   * Get the short signature for language 
	   * (letters extension :language 2 letters)
	   * @return string signature for language
	   */
	  public static String getLanguage()
	  {
		if (JUMPWorkbench.I18N_SETLOCALE == "") {
			// No locale has been specified at startup: choose default locale
			return rb.getLocale().getLanguage();
		}
		else {
	  	    return JUMPWorkbench.I18N_SETLOCALE.split("_")[0];
		}
	  }
	  
	  public String getMessage(Class clazz,String label, Object[] objects) {
	  	try {
				return rb != null ? 
						getMessage(clazz.getName() + "." + label, objects) : label;
			} catch (MissingResourceException e) {
				return label;
			}
	  }
	  
	  /**
	   * Process text with the locale 'jump_<locale>.properties' file
	   * If no resourcebundle is found, returns default string contained
	   * inside com.vividsolutions.jump.jump
	   * @param label with argument insertion : {0} 
	   * @param objects
	   * @return i18n label
	   */
	  public static String getMessage(String label, Object[] objects){
	      try {
	      MessageFormat mf = new MessageFormat(rb.getString(label));
	      return mf.format(objects);
	   } catch (java.util.MissingResourceException e) {
	         String default_translation=ResourceBundle.getBundle("com.vividsolutions.jump.jump").getString(label);
	         LOG.debug(e.getMessage()+" default_value:"+ default_translation);
	         MessageFormat mf = new MessageFormat(default_translation);
		     return mf.format(objects);
	      }
	  } 


	/**
	 * Process text with the locale 'pluginName_<locale>.properties' file
	 * 
	 * @param pluginName (path + name)
	 * @param label
	 * @return i18n label
	 */
	
	/*
	public static String get(String pluginName, String label)
	{
		if (LOG.isDebugEnabled()){
		  LOG.debug(I18N.plugInsResourceBundle.get(pluginName)+" "+label
				  + ((ResourceBundle)I18N.plugInsResourceBundle
						  .get(pluginName))
						  .getString(label));
		}
		  return ((ResourceBundle)I18N.plugInsResourceBundle
					  .get(pluginName))
					  .getString(label);
	}
	*/
	  
	/**
	 * Process text with the locale 'pluginName_<locale>.properties' file
	 * 
	 * @param pluginName (path + name)
	 * @param label with argument insertion : {0} 
	 * @param objects
	 * @return i18n label
	 */
	/*
	public static String getMessage(String pluginName, String label, Object[] objects){
		MessageFormat mf = new MessageFormat(((ResourceBundle)I18N.plugInsResourceBundle
											  .get(pluginName))
											  .getString(label));
		return mf.format(objects);
	}
	*/


}
