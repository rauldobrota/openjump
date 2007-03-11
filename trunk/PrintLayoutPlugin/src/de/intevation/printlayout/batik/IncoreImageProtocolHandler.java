/*
 * IncoreImageProtocolHandler.java
 * -------------------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.batik;

import org.apache.batik.util.AbstractParsedURLProtocolHandler;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.ParsedURLData;

import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.Iterator;

import java.io.InputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.imageio.ImageIO;

public final class IncoreImageProtocolHandler
extends            AbstractParsedURLProtocolHandler
{
	public static final String INCORE_IMAGE = "incore-image";

	private HashMap images;

	private int     uniqueId;

	private int     refCount;

	private static  IncoreImageProtocolHandler instance;

	/**
	 * Make it a singleton because only one be registered as a factory
	 * with ParseURL#registerHandler().
	 */
	private IncoreImageProtocolHandler() {
		super(INCORE_IMAGE);
	} 	

	public static synchronized IncoreImageProtocolHandler getInstance() {
		if (instance == null)
			instance = new IncoreImageProtocolHandler();
		return instance;
	}

	public synchronized void incrementReferenceCount() {
		++refCount;
	}

	public synchronized void decrementReferenceCount() {
		if (refCount > 0 && --refCount == 0) {
			//System.err.println("IncoreImageProtocolHandler: ref count went zero");
			if (images != null) {
				HashMap x = images;
				images = null;
				x.clear();
			}
		}
	}

	private static final class ServeImage
	extends                    ParsedURLData 
	{
		public ServeImage(String what) {
			path        = what;
			protocol    = INCORE_IMAGE;
			contentType = "image/png";
		}

		protected InputStream openStreamInternal(
			String   userAgent,
      Iterator mimeTypes,
      Iterator encodingTypes
		) 
		throws IOException
		{
			BufferedImage im;
			final BufferedImage image = getInstance().getImage(path);
			if (image == null)
				throw new IOException();

			final PipedOutputStream out = new PipedOutputStream();

			new Thread() {
				public void run() {
					try {
						ImageIO.write(image, "png", out);
					}
					catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}.start();

			return new PipedInputStream(out);
		}
	} // class ServeImage

	private String uniqueID() {
		int id = uniqueId++;
		return Integer.toString(id, 26);
	}

	public synchronized BufferedImage getImage(String id) {
		return images != null
			? (BufferedImage)images.get(id)
			: null;
	}


	public synchronized String storeImage(BufferedImage image) {
		return storeImage(image, null);
	}

	public synchronized String storeImage(
		BufferedImage image, 
		String        prefix
	) {
		String id = uniqueID();
		if (prefix != null)
			id = prefix + id;
		if (images == null)
			images = new HashMap();
		images.put(id, image);
		return id;
	}

	public synchronized BufferedImage removeImage(String id) {
		if (images == null)
			return null;
		BufferedImage image = (BufferedImage)images.remove(id);
		if (images.isEmpty())
			images = null;
		return image;
	}

	public ParsedURLData parseURL(ParsedURL basepurl, String urlStr) {
		return parseURL(urlStr);
	}

	public ParsedURLData parseURL(String urlStr) {
		String path = urlStr.substring(INCORE_IMAGE.length()+1);
		return new ServeImage(path);
	}
}
// end of file
