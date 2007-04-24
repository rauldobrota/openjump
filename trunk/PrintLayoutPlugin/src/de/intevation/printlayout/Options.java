/*
 * Options.java
 * ------------
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
import java.util.prefs.PreferenceChangeListener;
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
	 * Get boolean value for given key.
	 * @param key the key to look for
	 * @return the value, false if not found
	 */
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	
	/**
	 * Get boolean value for given key.
	 * @param key the key to look for
	 * @param def the default value
	 * @return the value, def if not found
	 */
	public boolean getBoolean(String key, boolean def) {
		String value = System.getProperty(key);

		if (value != null)
			return Boolean.valueOf(value.trim()).booleanValue();

		Preferences prefs = Preferences.userNodeForPackage(getClass());

		return (value = prefs.get(key, null)) != null
			? Boolean.valueOf(value.trim()).booleanValue() 
			: def;
	}

	/**
	 * Stores an integer value into the system properties
	 * and the preferences.
	 * @param key the key
	 * @param value the value
	 */
	public void putBoolean(String key, boolean value) {
		System.setProperty(key, Boolean.toString(value));
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		prefs.putBoolean(key, value);

		try {
			prefs.flush();
		}
		catch (BackingStoreException bse) {
			bse.printStackTrace();
		}
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
	
	/**
	 * Get double value for given key.
	 * @param key the key to look for
	 * @return the value, null if not found
	 */
	public Double getDouble(String key) {
		return getDouble(key, null);
	}

	/**
	 * Get double value for given key.
	 * @param key the key to look for
	 * @param def the default value
	 * @return the value, def if not found
	 */
	public Double getDouble(String key, Double def) {
		try {
			String value = System.getProperty(key);

			if (value != null)
				return new Double(value.trim());

			Preferences prefs = Preferences.userNodeForPackage(getClass());

			return (value = prefs.get(key, null)) != null
				? new Double(value.trim())
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
	public void putDouble(String key, Double value) {
		System.setProperty(key, value != null ? value.toString() : "");
		Preferences prefs = Preferences.userNodeForPackage(getClass());

		if (value == null)
			prefs.remove(key);
		else
			prefs.putDouble(key, value.doubleValue());

		try {
			prefs.flush();
		}
		catch (BackingStoreException bse) {
			bse.printStackTrace();
		}
	}

	public void addPreferenceChangeListener(PreferenceChangeListener listener) {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		prefs.addPreferenceChangeListener(listener);
	}
	
	public void removePreferenceChangeListener(
			PreferenceChangeListener listener
	) {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		prefs.removePreferenceChangeListener(listener);
	}
}
// end of file
