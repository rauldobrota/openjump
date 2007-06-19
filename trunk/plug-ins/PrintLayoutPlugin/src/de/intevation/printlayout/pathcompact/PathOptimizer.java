/*
 * PathOptimizer.java
 * ------------------
 * (c) 2007 by Sascha L. Teichmann (teichmann@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.pathcompact;

import org.apache.batik.parser.PathHandler;

import java.util.ArrayList;
import java.util.Locale;

import java.text.NumberFormat;

public class PathOptimizer
implements   PathHandler
{
	private static final int START     = 0;
	private static final int END       = 1;
	private static final int LINE      = 2;
	private static final int CLOSE     = 3;
	private static final int MOVE      = 4;
	private static final int REL_LINE  = 5;
	private static final int REL_MOVE  = 6;
	private static final int OTHER     = -1;

	private static final NumberFormat DELTA_FORMAT =
		NumberFormat.getInstance(Locale.US);

	static {
		DELTA_FORMAT.setMaximumFractionDigits(3);
		DELTA_FORMAT.setGroupingUsed(false);
	}

	private float posX;
	private float posY;

	private int last;

	private ArrayList segments;

	public PathOptimizer() {
		segments = new ArrayList(8*1024);
		last = OTHER;
	}

	public void clear() {
		segments.clear();
		last = OTHER;
		posX = posY = 0f;
	}

	public String generate() {
		int N = segments.size();
		int size = N;
		for (int i = 0; i < N; ++i)
			size += ((String)segments.get(i)).length();

		StringBuffer sb = new StringBuffer(size);

		String last;
		if (N > 0)
			sb.append(last = (String)segments.get(0));
		else
			last = null;

		for (int i = 1; i < N; ++i) {
			String s = (String)segments.get(i);
			if (Character.isLetter(s.charAt(0))) {
				if (Character.isLetter(last.charAt(last.length()-1)))
					sb.append(' ');
			}
			else
				sb.append(' ');
			sb.append(s);
			last = s;
		}
		return sb.toString();
	}

	protected final void emit(String segment) {
		segments.add(segment);
	}

	protected final void replaceLast(String segment) {
		segments.set(segments.size()-1, segment);
	}

	private static final String flag(boolean flag) {
		return flag ? "1" : "0";
	}

	/** Invoked when an absolute elliptical arc command has been parsed. */
	public void	arcAbs(
		float rx, float ry, 
		float xAxisRotation, 
		boolean largeArcFlag, 
		boolean sweepFlag, 
		float x, float y
	) {
		emit(
			"A" + rx + " " + ry + " " + 
			xAxisRotation + " " + flag(largeArcFlag) + " " + flag(sweepFlag) + " " +
			x + " " + y);
		posX = x;
		posY = y;
		last = OTHER;
	}

	/** Invoked when a relative elliptical arc command has been parsed. */
	public void	arcRel(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
		emit(
			"a" + rx + " " + ry + " " + 
			xAxisRotation + " " + flag(largeArcFlag) + " " + flag(sweepFlag) + " " +
			x + " " + y);
		posX += x;
		posY += y;
		last = OTHER;
	}

	/** Invoked when a closepath has been parsed. */
	public void	closePath() {
		emit("z");
		last = CLOSE;
	}

	/** Invoked when an absolute cubic bezier curve command has been parsed. */
	public void	curvetoCubicAbs(float x1, float y1, float x2, float y2, float x, float y) {
		posX = x;
		posY = y;
		emit("C" + x1 + " " + y1 + " " + x2 + " " + y2 + " "+ x + " " + y);
		last = OTHER;
	}

	/** Invoked when a relative cubic bezier curve command has been parsed. */
	public void	curvetoCubicRel(float x1, float y1, float x2, float y2, float x, float y) {
		posX += x;
		posY += y;
		emit("c" + x1 + " " + y1 + " "  +x2 + " " + y2 + " "+ x + " " + y);
		last = OTHER;
	}

	/** Invoked when an absolute smooth cubic bezier curve command has been parsed. */
	public void	curvetoCubicSmoothAbs(float x2, float y2, float x, float y) {
		posX = x;
		posY = y;
		emit("S" + x2 + " " + y2 + " "+ x + " " + y);
		last = OTHER;
	}

	/** Invoked when a relative smooth cubic bezier curve command has been parsed. */
	public void	curvetoCubicSmoothRel(float x2, float y2, float x, float y) {
		posX += x;
		posY += y;
		emit("s" + x2 + " " + y2 + " "+ x + " " + y);
		last = OTHER;
	}

	/** Invoked when an absolute quadratic bezier curve command has been parsed. */
	public void	curvetoQuadraticAbs(float x1, float y1, float x, float y) {
		posX = x;
		posY = y;
		emit("Q" + x1 + " " + y1 + " "+ x + " " + y);
		last = OTHER;
	}

	/** Invoked when a relative quadratic bezier curve command has been parsed. */
	public void	curvetoQuadraticRel(float x1, float y1, float x, float y) {
		posX += x;
		posY += y;
		emit("q" + x1 + " " + y1 + " "+ x + " " + y);
		last = OTHER;
	}

	/** Invoked when an absolute smooth quadratic bezier curve command has been parsed. */
	public void	curvetoQuadraticSmoothAbs(float x, float y) {
		posX = x;
		posY = y;
		emit("T" + x + " " + y);
		last = OTHER;
	}

	/** Invoked when a relative smooth quadratic bezier curve command has been parsed. */
	public void	curvetoQuadraticSmoothRel(float x, float y) {
		posX += x;
		posY += y;
		emit("t" + x + " " + y);
		last = OTHER;
	}

	/** Invoked when the path ends. */
	public void	endPath() {
		last = END;
	}

	/** Invoked when an absolute line command has been parsed. */
	public void	linetoAbs(float x, float y) {
		if (last == REL_LINE && x == posX && y == posY) {
			//++ignoredLines;
			return;
		}
		float dx = x - posX;
		float dy = y - posY;
		if (last == REL_LINE)
			emit(DELTA_FORMAT.format(dx) + " " + DELTA_FORMAT.format(dy));
		else {
			last = REL_LINE;
			emit("l" + DELTA_FORMAT.format(dx) + " " + DELTA_FORMAT.format(dy));
		}

		posX = x;
		posY = y;
	}

	/** Invoked when an horizontal absolute line command has been parsed. */
	public void	linetoHorizontalAbs(float x) {
		emit("H" + x);
		posX = x;
		last = OTHER;
	}

	/** Invoked when an horizontal relative line command has been parsed. */
	public void	linetoHorizontalRel(float x) {
		emit("h" + x);
		posX += x;
		last = OTHER;
	}

	/** Invoked when a relative line command has been parsed. */
	public void	linetoRel(float x, float y) {
		if (last == REL_LINE && x == 0f && y == 0f) {
			//++ignoredLines;
			return;
		}

		if (last == REL_LINE)
			emit(DELTA_FORMAT.format(x) + " " + DELTA_FORMAT.format(y));
		else {
			last = REL_LINE;
			emit("l" + DELTA_FORMAT.format(x) + " " + DELTA_FORMAT.format(y));
		}

		posX += x;
		posY += y;
	}

	/** Invoked when a vertical absolute line command has been parsed. */
	public void	linetoVerticalAbs(float y) {
		emit("V" + y);
		posY = y;
		last = OTHER;
	}

	/** Invoked when a vertical relative line command has been parsed. */
	public void	linetoVerticalRel(float y) {
		emit("v" + y);
		posY += y;
		last = OTHER;
	}

	/** Invoked when an absolute moveto command has been parsed. */
	public void	movetoAbs(float x, float y) {
		if (x == posX && y == posY && !segments.isEmpty()) {
			//++ignoredMoves;
		}
		else {
			if (last == MOVE) // last move was redundant
				replaceLast("M"  + x + " " + y);
			else {
				last = MOVE;
				emit("M" + x + " " + y);
			}
			posX = x;
			posY = y;
		}
	}
	
	/** Invoked when a relative moveto command has been parsed. */
	public void	movetoRel(float x, float y) {
		if (x == 0f && y == 0f && !segments.isEmpty()) {
			//++ignoredMoves;
		}
		else {
			if (last == REL_MOVE)
				emit(DELTA_FORMAT.format(x) + " " + DELTA_FORMAT.format(y));
			else {
				last = REL_MOVE;
				emit("m" + DELTA_FORMAT.format(x) + " " + DELTA_FORMAT.format(y));
			}
			posX += x;
			posY += y;
		}
	}

	/** Invoked when the path starts. */
	public void	startPath() {
		last = START;
	}
}
// end of file
