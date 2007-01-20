/*
 * BoxFactory.java
 * ---------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import java.awt.geom.GeneralPath;
import java.awt.geom.AffineTransform;

public final class Arrows
{
	public static final float [] COORDS = {
		-3f, -4f,
		-6f, -4f,
		 0f, -10f,
		 6f, -4f,
		 3f, -4f,
		 3f,  4f,
		 6f,  4f,
		 0f,  10f,
		-6f,  4f,
		-3f,  4f,
		-3f, -4f,
	};

	public static final GeneralPath createArrow(
		double zx, double zy,
		double angle,
		double scale,
		double dx, double dy
	) {
		AffineTransform transZero =
			AffineTransform.getTranslateInstance(zx, zy);

		AffineTransform rot =
			AffineTransform.getRotateInstance(angle);

		AffineTransform s =
			AffineTransform.getScaleInstance(scale, scale);

		AffineTransform trans = 
			AffineTransform.getTranslateInstance(dx, dy);

		rot.concatenate(transZero);
		s.concatenate(rot);
		trans.concatenate(s);

		float [] dst = new float[COORDS.length];

		trans.transform(COORDS, 0, dst, 0, dst.length >> 1);

		GeneralPath arrow = new GeneralPath();
		arrow.moveTo(dst[0], dst[1]);

		for (int i = 2; i < dst.length-2; i += 2)
			arrow.lineTo(dst[i], dst[i+1]);

		arrow.closePath();

		return arrow;
	}

	private Arrows() {
	}
}
// end of file
