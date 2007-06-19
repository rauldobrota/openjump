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

/**
 * Class used to offer i18n support.
 */
public class I18N
{
	private static final ResourceBundle I18N = 
		ResourceBundle.getBundle("de.intevation.printlayout.i18n.messages");

	private I18N() {
	}

	/**
	 * looks up a key in the i18n table.
	 * @param key the key to the i18n string.
	 * @return the i18n value of the key. key if not found.
	 */
	public static String getString(String key) {
		return getString(key, key);
	}

	/**
	 * looks up a key in the i18n table.
	 * @param key the key to the i18n string.
	 * @param def the default value if no entry is found.
	 * @return the i18n value of the key. def if not found.
	 */
	public static String getString(String key, String def) {
		try {
			String value = I18N.getString(key);
			return value != null ? value : def;
		}
		catch (MissingResourceException mre) {
			return def;
		}
	}

	/**
	 * extracts the name from a i18n string.
	 * i18n strings may contain &amp; chars to mark menu shortcuts.
	 * These shotcuts will be removed from name.
	 * @param label the string with &amp; shortcut markers.
	 * @return label without the shortcut markers.
	 */
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

	/**
	 * extracts a mnemonic from an i18n string.
	 * In menus a mnemonic can be set. these markers
	 * are prefixed with &amp; in the i18n string.
	 * @param label i18n label with &amp; markers.
	 * @return an integer constructed from the char
	 *         following the &amp;. null if no maker
	 *         was found.
	 */
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
