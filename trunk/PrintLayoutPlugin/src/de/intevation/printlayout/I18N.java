/*
 * DocumentManager.java
 * --------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N
{
	private static final ResourceBundle I18N = 
		ResourceBundle.getBundle("de.intevation.printlayout.i18n.messages");

	private I18N() {
	}

	public static String getString(String key) {
		return I18N.getString(key);
	}

	public static String getString(String key, String def) {
		try {
			return I18N.getString(key);
		}
		catch (MissingResourceException mre) {
			return def;
		}
	}
}
// end of file
