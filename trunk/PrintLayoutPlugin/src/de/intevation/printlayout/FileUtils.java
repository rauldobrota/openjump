/*
 * FileUtils.java
 * ----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileUtils {
	public static class MultiExtFileFilter extends FileFilter {
		protected String[] exts;
		protected String   desc;
		
		public MultiExtFileFilter(String[] possibleExtension, String description) {
			exts = possibleExtension;
		  desc = description;
		}
		
		public boolean accept(File f) {
			if (exts == null || exts.length == 0)
				return true;

			if (f.isDirectory())
				return true;
				
			return checkExtension(getExtension(f), exts);
		}

		public String getDescription() {
			return desc;
		}
	}

	public static File addExtIfNeeded(
			File f, 
			String[] possExts, 
			String defaultExt
	) {
		if (! checkExtension(getExtension(f), possExts))
			return new File(f.getAbsolutePath() + defaultExt);
		
		return f;
	}
	
	protected static String getExtension(File file) {
		String ext = null;
		String name = file.getName();
		int i =  name.lastIndexOf('.');

		if (i > 0 && i < name.length() -1) {
			ext = name.substring(i + 1).toLowerCase();
		}
		
		return ext;
	}

	protected static boolean checkExtension(String ext, String[] possExts) {
			for (int i = 0; i < possExts.length; i++) {
				if (possExts[i].equals(ext))
					return true;
			}
			
			return false;
	}
}
