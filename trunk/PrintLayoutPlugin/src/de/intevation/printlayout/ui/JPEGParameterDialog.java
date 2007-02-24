/*
 * JPEGParameterDialog.java
 * ------------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */

package de.intevation.printlayout.ui;

import java.awt.Frame;
import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JSlider;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JComponent;

import javax.swing.text.NumberFormatter;

import javax.swing.border.TitledBorder;

import java.text.NumberFormat;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.apache.batik.transcoder.Transcoder;

import org.apache.batik.transcoder.image.JPEGTranscoder;

import de.intevation.printlayout.I18N;

/**
 * Simple JDialog used to ask the user for the size and
 * the quality of the exported JPEG.
 */
public class JPEGParameterDialog
extends      ImageParameterDialog
{
	/**
	 * initial quality of the JPG
	 */
	public static final int INITIAL_QUALITY =  85;

	/**
	 * TextField containing the current quality.
	 */
	protected JFormattedTextField qualityTF;

	/**
	 * Slider containing the current quality.
	 */
	protected JSlider             quality;

	/**
	 * creates JPEG dialog
	 * @param owner owner frame
	 */
	public JPEGParameterDialog(Frame owner) {
		super(owner);
	}

	/**
	 * overrides super init() to set dialog title.
	 */
	protected void init() {
		setTitle(I18N.getString(
			"JPEGParameterDialog.Title", "JPEG export parameter"));
		super.init();
	}

	/**
	 * overrides createExtraComponents() to create a slider
	 * and a TextField containing the current quality.
	 */
	protected JComponent createExtraComponents() {
		JPanel qual = new JPanel(new FlowLayout(FlowLayout.CENTER));

		String qualS = I18N.getString(
    	"JPEGParameterDialog.Quality", "quality:");

		qual.setBorder(new TitledBorder(qualS));

		quality = new JSlider(JSlider.HORIZONTAL, 0, 100, INITIAL_QUALITY);
		quality.setMajorTickSpacing(25);
		quality.setMinorTickSpacing(5);
		quality.setPaintTicks(true);
		quality.setPaintLabels(true);

		quality.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				JSlider slider = (JSlider)ce.getSource();
				Integer value = new Integer(slider.getValue());
				if (!slider.getValueIsAdjusting())
					qualityTF.setValue(value);
				else
        	qualityTF.setText(value.toString());
			}
		});

		qual.add(quality);

		NumberFormat qFormat = NumberFormat.getIntegerInstance();
		qFormat.setGroupingUsed(false);
		NumberFormatter qFormatter = new NumberFormatter(qFormat);
		qFormatter.setMinimum(new Integer(0));
		qFormatter.setMaximum(new Integer(100));

		qualityTF = new JFormattedTextField(qFormatter);
		qualityTF.setColumns(3);
		qualityTF.setHorizontalAlignment(JFormattedTextField.TRAILING);

		qualityTF.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if ("value".equals(e.getPropertyName())) {
					Number value = (Number)e.getNewValue();
					if (quality != null && value != null)
						quality.setValue(new Integer(value.intValue()));
				}
			}
		});
		qualityTF.setValue(new Integer(INITIAL_QUALITY));
		qual.add(qualityTF);
		return qual;
	}

	/**
	 * overrides method from super class
	 * @return new JPEGTranscoder
	 */
	protected Transcoder createTrancoder() {
		JPEGTranscoder transcoder = new JPEGTranscoder();
		int size [] = new int[2];
		getImageSize(size);

		Float W = new Float((float)size[0]);
		Float H = new Float((float)size[1]);
		Float Q = new Float(quality.getValue()/100d);

		transcoder.addTranscodingHint(JPEGTranscoder.KEY_WIDTH,   W);
		transcoder.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT,  H);
		transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, Q);
		return transcoder;
	}
}
// end of file
