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
			String value = I18N.getString(key);
			return value != null ? value : def;
		}
		catch (MissingResourceException mre) {
			return def;
		}
	}

	public static String getName(String label) {
		StringBuffer sb = new StringBuffer();

		boolean lastWasQuoteAmp = false;
		for(int i = 0, N = label.length(); i < N; i++) {
			if(label.charAt(i) == '&' && lastWasQuoteAmp) {
				lastWasQuoteAmp = false;
				sb.append('&');
			}
			else if(label.charAt(i) == '&') {
				lastWasQuoteAmp = true;
			}
			else {
				lastWasQuoteAmp = false;
				sb.append(label.charAt(i));
			}
		}
		return sb.toString();
	}
	public static Integer getMnemonic(String label) {
		Character c = null;
		boolean lastWasQuoteAmp = false;

		for (int i = 0, N = label.length(); i < N; i++) {
			if (lastWasQuoteAmp && label.charAt(i) == '&') {
				lastWasQuoteAmp = false;
			}
			else if (lastWasQuoteAmp && Character.isLetter(label.charAt(i))) {
				c = new Character(Character.toUpperCase(label.charAt(i)));
				break;
			}
			else if(lastWasQuoteAmp) {
				lastWasQuoteAmp = false;
			}
			else if (label.charAt(i) == '&') {
				lastWasQuoteAmp = true;
			}
		}
		
		if(c != null)
			return new Integer((int) c.charValue());
		return null;
	}

}
// end of file
