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
		-3f,  -15f,
		-6f,  -15f,
		 0f, -21f,
		 6f,  -15f,
		 3f,  -15f,
		 3f,   -7f,
		 6f,   -7f,
		 0f,  -1f,
		-6f,   -7f,
		-3f,   -7f,
		-3f,  -15f,
	};

	public static final GeneralPath U_D = createArrow();

	public static final GeneralPath L_R =
		new GeneralPath(
			U_D.createTransformedShape(
				AffineTransform.getRotateInstance(Math.PI*0.5d)));

	public static final GeneralPath UL_LR =
		new GeneralPath(
			U_D.createTransformedShape(
				AffineTransform.getRotateInstance(Math.PI*(3d/4d))));

	public static final GeneralPath UR_LL =
		new GeneralPath(
			U_D.createTransformedShape(
				AffineTransform.getRotateInstance(Math.PI*0.25d)));

	public static final GeneralPath createArrow() {
		GeneralPath arrow = new GeneralPath();

		arrow.moveTo(COORDS[0], COORDS[1]);

		for (int i = 2; i < COORDS.length-2; i += 2)
			arrow.lineTo(COORDS[i], COORDS[i+1]);

		arrow.closePath();

		return arrow;
	}

	private Arrows() {
	}
}
// end of file
