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

/**
 * Extends javax.swing.filechooser.FileFilter to
 * filter directory listings in JFileChoosers
 * for a set of different extentions. e.g. *.jpg, *.jpeg
 * or *.tif, *.tiff.
 */
public class FileFilter
extends      javax.swing.filechooser.FileFilter
{
	/**
	 * accepted extentions (lower cased)
	 */
	protected String[] exts;

	/**
	 * the description of this filter
	 */
	protected String   desc;
	
	/**
	 * Convenience constructor for only one extension.
	 * @param extension the extention to be accepted
	 * @param description the description of this filter
	 */
	public FileFilter(String extension, String description) {
		this(new String [] { extension }, description);
	}

	/**
	 * Creates a filter for a list of extensions with a given
	 * description.
	 * @param extensions the list of extensions
	 * @param description the description
	 */
	public FileFilter(String[] extensions, String description) {
		if (extensions != null && extensions.length > 0) {
			exts = new String[extensions.length];
			for (int i = 0; i < exts.length; ++i)
				exts[i] = extensions[i].toLowerCase();
		}
		desc = description;
	}
	
	/**
	 * used by JFileChooser to filter out certain files.
	 * @param file the file to check
	 * @return true if file should be shown else false
	 */
	public boolean accept(File file) {
		return exts == null
			||   exts.length == 0
			||   file.isDirectory()
			||   checkExtension(getExtension(file));
	}

	/**
	 * The description of this filter.
	 * @return the description
	 */
	public String getDescription() {
		return desc;
	}

	/**
	 * looks if the given file ends with one of the
	 * file extension used by this filter. If
	 * it does the file name is returned without any
	 * modification else the first extension is
	 * appended to the filename.
	 * @param file the file name to check
	 * @return the extended file name (if needed)
	 */
	public File addExtIfNeeded(File file) {
		return exts != null && exts.length > 0
			? addExtIfNeeded(file, exts[0])
			: file;
	}

	/**
	 * looks if the given file ends with one of the
	 * file extension used by this filter. If
	 * it does the file name is returned without any
	 * modification else defaultExt is appended to the filename.
	 * @param file the file name to check
	 * @param defaultExt the extension to be added if check fails
	 * @return the extended file name (if needed)
	 */
	public File addExtIfNeeded(File file, String defaultExt) {
		return checkExtension(getExtension(file))
			? file
			: new File(file.getAbsolutePath() + "." + defaultExt);
	}
	
	/**
	 * static helper to extract the extension of a file name.
	 * @param file the file name
	 * @return the lower cased extension of the file if any.<br>
	 *          else null
	 */
	protected static String getExtension(File file) {
		String name = file.getName();
		int i =  name.lastIndexOf('.');

		return i > 0 && i < name.length() -1 
			? name.substring(i + 1).toLowerCase()
			: null;
	}

	/**
	 * looks if the given extension is in list of
	 * used extensions.
	 * @param ext the extension (needs to be lower cased)
	 * @return true if ext is in list, else false.
	 */
	protected boolean checkExtension(String ext) {
		if (ext != null)
			for (int i = 0; i < exts.length; i++) 
				if (exts[i].equals(ext))
					return true;
			
		return false;
	}
}
// end of file
