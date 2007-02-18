/*
 * FileFilter.java
 * ---------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.util;

import java.io.File;

public class FileFilter
extends      javax.swing.filechooser.FileFilter
{
	protected String[] exts;
	protected String   desc;
	
	public FileFilter(String possibleExtension, String description) {
		this(new String [] { possibleExtension }, description);
	}

	public FileFilter(String[] possibleExtension, String description) {
		exts = (String [])possibleExtension.clone();
		for (int i = 0; i < exts.length; ++i)
			exts[i] = exts[i].toLowerCase();
		desc = description;
	}
	
	public boolean accept(File f) {
		if (exts == null || exts.length == 0)
			return true;

		if (f.isDirectory())
			return true;
			
		return checkExtension(getExtension(f));
	}

	public String getDescription() {
		return desc;
	}

	public File addExtIfNeeded(File f) {
		return exts != null && exts.length > 0
			? addExtIfNeeded(f, exts[0])
			: f;
	}

	public File addExtIfNeeded(File f, String defaultExt) {
		if (!checkExtension(getExtension(f)))
			return new File(f.getAbsolutePath() + "." + defaultExt);
		return f;
	}
	
	protected static String getExtension(File file) {
		String name = file.getName();
		int i =  name.lastIndexOf('.');

		return i > 0 && i < name.length() -1 
			? name.substring(i + 1).toLowerCase()
			: null;
	}

	protected boolean checkExtension(String ext) {
		for (int i = 0; i < exts.length; i++) 
			if (exts[i].equals(ext))
				return true;
			
		return false;
	}
}
// end of file
