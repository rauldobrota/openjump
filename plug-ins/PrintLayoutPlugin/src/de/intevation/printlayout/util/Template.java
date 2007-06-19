/*
 * Template.java
 * -------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.util;

import java.util.HashMap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A small string templating class.
 * It replaces ${VARIABLE} or ${VARIABLE:DEFAULT}
 * expression in a string with the text of predefined
 * variables. ${VARIABLE} will be replaced by
 * the found text if any. If no replacement text is 
 * found the '${VARIABLE}' text is left in place.
 * The ${VARIABLE:DEFAULT} is handled the same only
 * if the replacement text for ${VARIABLE} is not 
 * found the value of 'DEFAULT' is put instead.
 */
public class Template
{
	/**
	 * regular expression to catch a variable in the text
	 */
  public static final Pattern VARIABLE =
    Pattern.compile("\\$\\{([^\\}]+)\\}");

	/**
	 * regular expression to catch a VARIABLE:DEFAULT construct
	 * within variables.
	 */
  public static final Pattern DOUBLE_COLON =
    Pattern.compile("^([^:]+):(.+)$");

	/**
	 * a map of the defined variables.
	 */
  protected HashMap variables;

	/**
	 * constructs a Template with no variables.
	 */
	public Template() {
	}

	/**
	 * fetches the value of variable associated to key.
	 * @param key name of the variable.
	 * @return value of the variable. null if not defined.
	 */
  public String getVariable(String key) {
    return getVariable(key, null);
  }

	/**
	 * fetches the value of variable associated to key.
	 * @param key name of the variable.
	 * @param def default value.
	 * @return value of the variable. default value if not defined.
	 */
  public String getVariable(String key, String def) {
    if (variables == null) return def;
    String x = (String)variables.get(key);
    return x != null ? x : def;
  }

	/**
	 * sets a variable to a given value.
	 * @param key the name of the variable
	 * @param value the value of the variable
	 */
  public String setVariable(String key, String value) {
    if (variables == null)
      variables = new HashMap();
    return (String)variables.put(key, value);
  }

	/**
	 * removes all variables.
	 */
  public void clear() {
    if (variables != null) {
      variables.clear();
      variables = null;
    }
  }

	/**
	 * applying the Template to a given string.
	 * All variables in the text are replaced by 
	 * the defined one of this Template.
	 * @param src source text with unreplaced variables.
	 * @return the text with the expanded variables.
	 */
	public String toString(String src) {

		StringBuffer sb = new StringBuffer();
    Matcher m = VARIABLE.matcher(src);
    while (m.find()) {
      String txt = m.group(1);
      Matcher m2 = DOUBLE_COLON.matcher(txt);
      String def;
      if (m2.matches()) {
        txt = m2.group(1);
        def = m2.group(2);
      }
      else
        def = null;
      String var = getVariable(txt, def);
			m.appendReplacement(sb, var == null
				? "\\$\\{" + txt + "\\}"
				: var);
    }
    m.appendTail(sb);
    return sb.toString();
	}
}
// end of file
