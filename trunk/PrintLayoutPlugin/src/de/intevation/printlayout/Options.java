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

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

/**
 * This class wraps the retrieval of preferences
 * values. First we have a look in the system properties.
 * If the requested value is there it is used.
 * If its not there we have a look if the requested
 * value is given as a preference. If the
 * value isn't there either the default value is taken.
 */
public class Options
{
	protected static Options instance;

	protected Options() {
	}

	/**
	 * Get the singleton
	 * @return the singleton of the options
	 */
	public static Options getInstance() {
		if (instance == null)
			instance = new Options();
		return instance;
	}

	/**
	 * Get integer value for given key.
	 * @param key the key to look for
	 * @return the value, null if not found
	 */
	public Integer getInteger(String key) {
		return getInteger(key, null);
	}

	/**
	 * Get integer value for given key.
	 * @param key the key to look for
	 * @param def the default value
	 * @return the value, def if not found
	 */
	public Integer getInteger(String key, Integer def) {
		try {
			String value = System.getProperty(key);

			if (value != null)
				return new Integer(value.trim());

			Preferences prefs = Preferences.userNodeForPackage(getClass());

			return (value = prefs.get(key, null)) != null
				? new Integer(value.trim())
				: def;
		}
		catch (NumberFormatException nfe) {
			return def;
		}
	}

	/**
	 * Stores an integer value into the system properties
	 * and the preferences.
	 * @param key the key
	 * @param value the value
	 */
	public void putInteger(String key, Integer value) {
		System.setProperty(key, value != null ? value.toString() : "");
		Preferences prefs = Preferences.userNodeForPackage(getClass());

		if (value == null)
			prefs.remove(key);
		else
			prefs.putInt(key, value.intValue());

		try {
			prefs.flush();
		}
		catch (BackingStoreException bse) {
			bse.printStackTrace();
		}
	}
}
// end of file
