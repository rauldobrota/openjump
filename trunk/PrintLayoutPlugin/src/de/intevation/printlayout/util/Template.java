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
 
public class Template
{
  public static final Pattern VARIABLE =
    Pattern.compile("\\$\\{([^\\}]+)\\}");

  public static final Pattern DOUBLE_COLON =
    Pattern.compile("^([^:]+):(.+)$");

  protected HashMap variables;

	public Template() {
	}

  public String getVariable(String key) {
    return getVariable(key, null);
  }

  public String getVariable(String key, String def) {
    if (variables == null) return def;
    String x = (String)variables.get(key);
    return x != null ? x : def;
  }

  public String setVariable(String key, String value) {
    if (variables == null)
      variables = new HashMap();
    return (String)variables.put(key, value);
  }

  public void clear() {
    if (variables != null) {
      variables.clear();
      variables = null;
    }
  }

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
